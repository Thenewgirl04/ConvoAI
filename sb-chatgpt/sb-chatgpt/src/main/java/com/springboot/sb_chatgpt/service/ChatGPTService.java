package com.springboot.sb_chatgpt.service;
import com.springboot.sb_chatgpt.dto.ChatGPTRequest;
import com.springboot.sb_chatgpt.dto.ChatGPTResponse;
import com.springboot.sb_chatgpt.dto.ChatResponse;
import com.springboot.sb_chatgpt.dto.PromptRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatGPTService {

    private final RestClient restClient;

    @Autowired
    public ChatGPTService(RestClient restClient){
        this.restClient = restClient;
    }

    @Value("${openapi.api.key}")
    private String apiKey;

    @Value("${openapi.api.model}")
    private String model;

    @Value("${openapi.api.url}")   // e.g. https://api.openai.com/v1/chat/completions
    private String apiUrl;

    private final Map<String, List<ChatGPTRequest.Message>> sessions = new ConcurrentHashMap<>();


    public ChatResponse getChatResponse(PromptRequest promptRequest) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Missing openapi.api.key");
        }
        if (promptRequest == null || promptRequest.prompt() == null || promptRequest.prompt().isBlank()) {
            return new ChatResponse(null, "Please provide a prompt.");
        }

        String sessionId = (promptRequest.sessionId() != null && !promptRequest.sessionId().isBlank())
                ? promptRequest.sessionId()
                : UUID.randomUUID().toString();

        sessions.putIfAbsent(sessionId, new ArrayList<>());

        if (sessions.get(sessionId).isEmpty()) {

            String therapyPrompt = """
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

            String defaultPrompt = "You are a helpful, concise assistant.";

            String systemPrompt = "therapy".equalsIgnoreCase(promptRequest.mode())
                    ? therapyPrompt
                    : defaultPrompt;
            sessions.get(sessionId).add(new ChatGPTRequest.Message("system", systemPrompt));
        }

        sessions.get(sessionId).add(new ChatGPTRequest.Message("user", promptRequest.prompt()));

        ChatGPTRequest chatGPTRequest = new ChatGPTRequest(model, sessions.get(sessionId));

        try {

            ChatGPTResponse response = restClient.post()
                    .uri("/chat/completions")
                    .body(chatGPTRequest)
                    .retrieve()
                    .body(ChatGPTResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()
                    || response.choices().get(0).message() == null) {
                return new ChatResponse(sessionId, "No content returned.");
            }

            String reply = response.choices().get(0).message().content();

            sessions.get(sessionId).add(new ChatGPTRequest.Message("assistant", reply));

            return new ChatResponse(sessionId, reply);

        } catch (org.springframework.web.client.RestClientResponseException e) {
            return new ChatResponse(sessionId, "Upstream error " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
        } catch (Exception e) {
            return new ChatResponse(sessionId, "Server error: " + e.getMessage());
        }
    }
}
