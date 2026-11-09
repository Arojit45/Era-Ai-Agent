package com.beacon.spring_agent.Client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MailAddressDto(
        @JsonProperty("Name") String name,
        @JsonProperty("Address") String address
) {
}
