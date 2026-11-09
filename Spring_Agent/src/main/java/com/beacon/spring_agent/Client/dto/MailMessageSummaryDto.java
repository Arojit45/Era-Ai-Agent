package com.beacon.spring_agent.Client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record MailMessageSummaryDto(
        @JsonProperty("ID") String id,
        @JsonProperty("MessageID") String messageId,
        @JsonProperty("Read") boolean read,
        @JsonProperty("From") MailAddressDto from,
        @JsonProperty("Subject") String subject,
        @JsonProperty("Created") String created
){
}
