package com.springboot.sb_chatgpt.dto;

import java.util.List;

public record SessionHistoryResponse(String sessionId, String mode, List<HistoryMessage> messages) {
}
