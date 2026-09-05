package com.springboot.sb_chatgpt.controller;

import com.springboot.sb_chatgpt.dto.ChatResponse;
import com.springboot.sb_chatgpt.dto.PromptRequest;
import com.springboot.sb_chatgpt.dto.SessionHistoryResponse;
import com.springboot.sb_chatgpt.dto.SessionSummaryResponse;
import com.springboot.sb_chatgpt.service.ChatGPTService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api")
public class ChatGPTController {

    private final ChatGPTService chatGPTService;

    public ChatGPTController(ChatGPTService chatGPTService) {
        this.chatGPTService = chatGPTService;
    }

    @GetMapping("/sessions")
    public List<SessionSummaryResponse> listSessions(@RequestParam String clientId) {
        return chatGPTService.listSessions(clientId);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(
            @PathVariable String sessionId,
            @RequestParam String clientId
    ) {
        return chatGPTService.deleteSession(sessionId, clientId)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/chat/{sessionId}")
    public ResponseEntity<SessionHistoryResponse> getSessionHistory(
            @PathVariable String sessionId,
            @RequestParam String clientId
    ) {
        return chatGPTService.getSessionHistory(sessionId, clientId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody PromptRequest promptRequest) {
        return chatGPTService.getChatResponse(promptRequest);
    }
}
