package com.beacon.spring_agent.Client;

import com.beacon.spring_agent.Client.dto.MailMessageDto;
import com.beacon.spring_agent.Client.dto.MailMessageResDto;
import com.beacon.spring_agent.Client.dto.MailMessageSummaryDto;
import com.beacon.spring_agent.Config.InboxProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class MailClient {
    private final RestClient restClient;
    private final String inboxProperties;


    public MailClient(RestClient.Builder restClient, InboxProperties probs) {
        this.restClient = restClient.baseUrl(probs.baseUrl()).build();
        this.inboxProperties = probs.address();
    }

    //To Read the Unread mail from the MailBox
    public List<MailMessageSummaryDto> listUnread(int limit){
        String query = "is:unread to:%s !from:%s".formatted(inboxProperties, inboxProperties);
        MailMessageResDto mailMessageResDto = restClient.get()
                .uri(url->url.path("/api/v1/search")
                        .queryParam("query",query)
                        .queryParam("limit",limit)
                        .build())
                .retrieve().body(MailMessageResDto.class);
        return (mailMessageResDto!=null)?mailMessageResDto.messages():List.of();
    }

    public MailMessageDto getMessage(String id) {
        return restClient.get()
                .uri("/api/v1/message/{id}", id)
                .retrieve()
                .body(MailMessageDto.class);
    }
    /**
     * Set the read flag for a single message. Used to put a message back to
     * unread when processing fails, so it is retried on the next poll.
     */
    public void setRead(String id, boolean read) {
        restClient.put()
                .uri("/api/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ReadUpdate(List.of(id), read))
                .retrieve()
                .toBodilessEntity();
    }
    private record ReadUpdate(
            @JsonProperty("IDs") List<String> ids,
            @JsonProperty("Read") boolean read
    ) {
    }



}
