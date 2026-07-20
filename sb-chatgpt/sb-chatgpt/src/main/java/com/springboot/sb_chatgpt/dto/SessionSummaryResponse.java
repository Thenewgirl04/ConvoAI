package com.springboot.sb_chatgpt.dto;

import java.time.Instant;

public record SessionSummaryResponse(
        String sessionId,
        String title,
        String mode,
        Instant createdAt,
        Instant lastActiveAt
) {
}
