package com.beacon.spring_agent;

import com.beacon.spring_agent.Client.dto.MailAddressDto;
import com.beacon.spring_agent.Client.dto.MailMessageDto;
import com.beacon.spring_agent.dto.IncomingEmailDto;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

@Component
public class Mapper {
    public IncomingEmailDto toIncomingMessage(MailMessageDto mailMessageDto){
        String from = (mailMessageDto.from()!=null)?mailMessageDto.from().address():"(Unknown)";
        List<String> to = mailMessageDto.to().stream().map(MailAddressDto::address).toList();
        String subject = mailMessageDto.subject()!=null? mailMessageDto.subject() : "";
        String body = bestBody(mailMessageDto);
        Instant receivedAt = parseDate(mailMessageDto.date());
        return new IncomingEmailDto(mailMessageDto.messageId(),from,to,subject,body,receivedAt);
    }
    private String bestBody(MailMessageDto message) {
        if (message.text() != null && !message.text().isBlank()) {
            return message.text().strip();
        }
        return message.html() != null ? message.html().strip() : "";
    }
    private Instant parseDate(String date) {
        if (date == null || date.isBlank()) {
            return Instant.now();
        }
        try {
            return OffsetDateTime.parse(date).toInstant();
        } catch (Exception e) {
            try {
                return Instant.parse(date);
            } catch (Exception ex) {
                return Instant.now();
            }
        }
    }

}
