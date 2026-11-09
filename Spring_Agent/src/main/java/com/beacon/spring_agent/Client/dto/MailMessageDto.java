package com.beacon.spring_agent.Client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record MailMessageDto(
        @JsonProperty("ID") String id,
        @JsonProperty("MessageID") String messageId,
        @JsonProperty("From") MailAddressDto from,
        @JsonProperty("To") List<MailAddressDto> to,
        @JsonProperty("Subject") String subject,
        @JsonProperty("Date") String date,
        @JsonProperty("Text") String text,
        @JsonProperty("HTML") String html
) {
    public List<MailAddressDto> to() {
        return to != null ? to : List.of();
    }
}
