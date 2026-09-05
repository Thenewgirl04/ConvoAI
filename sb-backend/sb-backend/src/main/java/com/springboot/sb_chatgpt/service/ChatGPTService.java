package com.springboot.sb_chatgpt.service;

import com.springboot.sb_chatgpt.dto.ChatGPTRequest;
import com.springboot.sb_chatgpt.dto.ChatGPTResponse;
import com.springboot.sb_chatgpt.dto.ChatResponse;
import com.springboot.sb_chatgpt.dto.HistoryMessage;
import com.springboot.sb_chatgpt.dto.PromptRequest;
import com.springboot.sb_chatgpt.dto.SessionHistoryResponse;
import com.springboot.sb_chatgpt.dto.SessionSummaryResponse;
import com.springboot.sb_chatgpt.entity.ChatMessageEntity;
import com.springboot.sb_chatgpt.entity.ChatSessionEntity;
import com.springboot.sb_chatgpt.entity.SessionMode;
import com.springboot.sb_chatgpt.repository.ChatSessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ChatGPTService {

    private static final String THERAPY_PROMPT = """
            You are a supportive, non-judgemental listener. You are NOT a licensed clinician.
            Style: brief reflections, open questions, warmth, concise.
            Reply: steps:
            1) Reflect what you heard in one sentence.
            2) Ask one open question to deepen understanding.
            3) Offer one short coping idea ONLY is the user asks or gives permission.
            4) If user shows signs of improvement, you may proceed to conclude the conversation.
            Boundaries: no diagnoses, no medical advice.
            Crisis: if you detect imminent self-harm or harm to others, stop normal replies and show he crisis card (988 in the U.S).
            """;

    private static final String DEFAULT_PROMPT = "You are a helpful, concise assistant.";
    private static final int TITLE_MAX_LENGTH = 50;

    private final RestClient restClient;
    private final ChatSessionRepository sessionRepository;

    @Value("${OPENAI_API_KEY:}")
    private String apiKey;

    @Value("${openapi.api.model}")
    private String model;

    public ChatGPTService(RestClient restClient, ChatSessionRepository sessionRepository) {
        this.restClient = restClient;
        this.sessionRepository = sessionRepository;
    }

    @Transactional(readOnly = true)
    public List<SessionSummaryResponse> listSessions(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return List.of();
        }

        return sessionRepository.findByClientIdOrderByLastActiveAtDesc(clientId).stream()
                .map(session -> new SessionSummaryResponse(
                        session.getId(),
                        session.getTitle(),
                        session.getMode(),
                        session.getCreatedAt(),
                        session.getLastActiveAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<SessionHistoryResponse> getSessionHistory(String sessionId, String clientId) {
        if (sessionId == null || sessionId.isBlank() || clientId == null || clientId.isBlank()) {
            return Optional.empty();
        }

        return sessionRepository.findByIdAndClientId(sessionId, clientId)
                .map(this::toHistoryResponse);
    }

    @Transactional
    public boolean deleteSession(String sessionId, String clientId) {
        if (sessionId == null || sessionId.isBlank() || clientId == null || clientId.isBlank()) {
            return false;
        }

        return sessionRepository.findByIdAndClientId(sessionId, clientId)
                .map(session -> {
                    sessionRepository.delete(session);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public ChatResponse getChatResponse(PromptRequest promptRequest) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Missing OPENAI_API_KEY");
        }
        if (promptRequest == null || promptRequest.prompt() == null || promptRequest.prompt().isBlank()) {
            return new ChatResponse(null, "Please provide a prompt.", null);
        }
        if (promptRequest.clientId() == null || promptRequest.clientId().isBlank()) {
            return new ChatResponse(null, "Please provide a clientId.", null);
        }

        String requestedMode = SessionMode.normalize(promptRequest.mode());
        String clientId = promptRequest.clientId();

        String sessionId = (promptRequest.sessionId() != null && !promptRequest.sessionId().isBlank())
                ? promptRequest.sessionId()
                : UUID.randomUUID().toString();

        ChatSessionEntity session = sessionRepository.findByIdAndClientId(sessionId, clientId).orElse(null);

        if (session == null && sessionRepository.existsById(sessionId)) {
            return new ChatResponse(sessionId, "Session not found for this client.", null);
        }

        if (session == null) {
            session = new ChatSessionEntity(sessionId, clientId, requestedMode, "New conversation");
            session.addMessage(new ChatMessageEntity("system", systemPromptFor(requestedMode)));
            session = sessionRepository.save(session);
        } else if (!session.getMode().equals(requestedMode)) {
            return new ChatResponse(
                    sessionId,
                    "This session is locked to " + session.getMode() + " mode. Start a new session to switch modes.",
                    session.getMode()
            );
        }

        session.addMessage(new ChatMessageEntity("user", promptRequest.prompt()));
        updateTitleFromFirstMessage(session, promptRequest.prompt());
        session.touch();

        List<ChatGPTRequest.Message> openAiMessages = session.getMessages().stream()
                .map(message -> new ChatGPTRequest.Message(message.getRole(), message.getContent()))
                .toList();

        ChatGPTRequest chatGPTRequest = new ChatGPTRequest(model, openAiMessages);

        try {
            ChatGPTResponse response = restClient.post()
                    .uri("/chat/completions")
                    .body(chatGPTRequest)
                    .retrieve()
                    .body(ChatGPTResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()
                    || response.choices().get(0).message() == null) {
                sessionRepository.save(session);
                return new ChatResponse(sessionId, "No content returned.", session.getMode());
            }

            String reply = response.choices().get(0).message().content();
            session.addMessage(new ChatMessageEntity("assistant", reply));
            sessionRepository.save(session);

            return new ChatResponse(sessionId, reply, session.getMode());

        } catch (org.springframework.web.client.RestClientResponseException e) {
            sessionRepository.save(session);
            return new ChatResponse(sessionId, "Upstream error " + e.getStatusCode() + ": " + e.getResponseBodyAsString(), session.getMode());
        } catch (Exception e) {
            sessionRepository.save(session);
            return new ChatResponse(sessionId, "Server error: " + e.getMessage(), session.getMode());
        }
    }

    private SessionHistoryResponse toHistoryResponse(ChatSessionEntity session) {
        List<HistoryMessage> history = new ArrayList<>();
        for (ChatMessageEntity message : session.getMessages()) {
            if ("user".equals(message.getRole()) || "assistant".equals(message.getRole())) {
                history.add(new HistoryMessage(message.getRole(), message.getContent()));
            }
        }
        return new SessionHistoryResponse(session.getId(), session.getMode(), history);
    }

    private String systemPromptFor(String mode) {
        return SessionMode.THERAPY.equals(mode) ? THERAPY_PROMPT : DEFAULT_PROMPT;
    }

    private void updateTitleFromFirstMessage(ChatSessionEntity session, String prompt) {
        long userMessageCount = session.getMessages().stream()
                .filter(message -> "user".equals(message.getRole()))
                .count();

        if (userMessageCount == 1) {
            session.setTitle(buildTitle(prompt));
        }
    }

    private String buildTitle(String prompt) {
        String trimmed = prompt.trim().replaceAll("\\s+", " ");
        if (trimmed.length() <= TITLE_MAX_LENGTH) {
            return trimmed;
        }
        return trimmed.substring(0, TITLE_MAX_LENGTH - 3) + "...";
    }
}
