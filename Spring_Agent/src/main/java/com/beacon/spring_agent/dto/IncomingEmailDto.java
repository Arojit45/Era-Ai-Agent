package com.beacon.spring_agent.dto;

import java.time.Instant;
import java.util.List;

public record IncomingEmailDto(
        String messageId,
        String from,
        List<String> to,
        String subject,
        String body,
        Instant receivedAt
) {
}
