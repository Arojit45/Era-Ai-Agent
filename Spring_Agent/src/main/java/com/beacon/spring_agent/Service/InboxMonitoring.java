package com.beacon.spring_agent.Service;

import com.beacon.spring_agent.Client.MailClient;
import com.beacon.spring_agent.Client.dto.MailMessageDto;
import com.beacon.spring_agent.Client.dto.MailMessageSummaryDto;
import com.beacon.spring_agent.Config.InboxProperties;
import com.beacon.spring_agent.Mapper;
import com.beacon.spring_agent.dto.IncomingEmailDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InboxMonitoring {
    private final EmailHandler emailHandler;
    private final MailClient mail;
    private final InboxProperties inboxProperties;
    private final Mapper mapper;

    @Scheduled(fixedDelayString = "${support-agent.inbox.poll-interval:10000}")
    public void poll(){
        try {
            List<MailMessageSummaryDto> unread = mail.listUnread(inboxProperties.batchSize());
            if (unread == null || unread.isEmpty()) return;
            for (MailMessageSummaryDto summary : unread) {
                processOne(summary.id());
            }
        }
        catch (Exception e) {
            // Mailpit may be starting up or briefly unreachable. Log and let the
            // next scheduled poll retry rather than killing the scheduler.
            log.warn("Inbox poll failed: {}", e.getMessage(), e);
        }
    }
    private void processOne(String id){
        try {
            MailMessageDto mailMessageDto = mail.getMessage(id);
            IncomingEmailDto incomingEmailDto = mapper.toIncomingMessage(mailMessageDto);
            boolean handle = emailHandler.handle(incomingEmailDto);
            if (!handle) {
                mail.setRead(id, false);
            }
        }
        catch (Exception e) {
            log.error("Failed to process message {}; resetting to unread for retry", id, e);
            try {
                mail.setRead(id, false);
            } catch (Exception reset) {
                log.warn("Could not reset message {} to unread: {}", id, reset.getMessage());
            }
        }
    }
}
