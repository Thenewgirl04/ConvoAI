package com.springboot.sb_chatgpt.controller;

import com.springboot.sb_chatgpt.dto.ChatResponse;
import com.springboot.sb_chatgpt.dto.PromptRequest;
import com.springboot.sb_chatgpt.service.ChatGPTService;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/chat")
public class ChatGPTController {

    private final ChatGPTService chatGPTService;

    public ChatGPTController(ChatGPTService chatGPTService){
        this.chatGPTService = chatGPTService;
    }

    @PostMapping
    public ChatResponse chat(@RequestBody PromptRequest promptRequest) {
        return chatGPTService.getChatResponse(promptRequest);
    }
}
