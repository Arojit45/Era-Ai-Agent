package com.beacon.spring_agent.Client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record MailMessageResDto(
        @JsonProperty("total") int total,
        @JsonProperty("unread") int unread,
        @JsonProperty("count") int count,
        @JsonProperty("messages") List<MailMessageSummaryDto> messages
) {
    public List<MailMessageSummaryDto> messages() {
        return messages != null ? messages : List.of();
    }
}
