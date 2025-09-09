package com.springboot.sb_chatgpt.dto;

public record PromptRequest(String prompt, String mode, String sessionId) {
}
