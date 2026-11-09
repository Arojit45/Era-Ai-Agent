package com.beacon.spring_agent.Service;

import com.beacon.spring_agent.Config.InboxProperties;
import com.beacon.spring_agent.dto.AgentResponseDto;
import com.beacon.spring_agent.dto.IncomingEmailDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportEmailSender {
    private static final DateTimeFormatter QUOTE_DATE =
            DateTimeFormatter.ofPattern("EEE, d MMM yyyy 'at' HH:mm", Locale.ENGLISH)
                    .withZone(ZoneId.systemDefault());
    private final JavaMailSender sender;
    private final InboxProperties properties;

    public boolean sendReplay(IncomingEmailDto emailDto, AgentResponseDto response) throws MessagingException {
        String recipient = emailDto.from();
        if (recipient == null || recipient.isBlank() || "(unknown)".equals(recipient)) {
            log.warn("No usable sender address on email \"{}\"; skipping reply", emailDto.subject());
            return false;
        }
        if (response.replyBody() == null || response.replyBody().isBlank()) {
            log.warn("Agent produced no reply body for email from {}; skipping reply", recipient);
            return false;
        }

        MimeMessage mime = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mime, false, "UTF-8");
        helper.setFrom(properties.address());
        helper.setTo(recipient);
        helper.setSubject(replySubject(emailDto, response));
        helper.setText(quoteOriginal(emailDto, response.replyBody()));

        // Thread the reply onto the original message when we know its Message-ID.
        String messageId = emailDto.messageId();
        if (messageId != null && !messageId.isBlank()) {
            String ref = messageId.startsWith("<") ? messageId : "<" + messageId + ">";
            mime.setHeader("In-Reply-To", ref);
            mime.setHeader("References", ref);
        }

        sender.send(mime);
        log.info("Replied to {} (subject: \"{}\")", recipient, mime.getSubject());
        return true;
    }

    /**
     * Build the reply subject from the original ("Re: <subject>"), which — together
     * with the In-Reply-To/References headers — is what makes mail clients group the
     * reply into the original conversation instead of showing a fresh thread.
     */
    private String replySubject(IncomingEmailDto original, AgentResponseDto response) {
        String base = original.subject() != null && !original.subject().isBlank()
                ? original.subject()
                : (response.replySubject() != null ? response.replySubject() : "");
        return base.regionMatches(true, 0, "Re:", 0, 3) ? base : "Re: " + base;
    }

    /**
     * Append the customer's original message under the agent's reply as a quoted
     * block ("On &lt;date&gt;, &lt;sender&gt; wrote: &gt; ..."), the way a normal mail
     * client does, so the reply carries the context it is responding to.
     */
    private String quoteOriginal(IncomingEmailDto original, String replyBody) {
        String body = original.body() != null ? original.body() : "";
        String quoted = body.isBlank()
                ? ""
                : body.stripTrailing().lines().map(line -> "> " + line).reduce((a, b) -> a + "\n" + b).orElse("");
        return """
                %s

                On %s, %s wrote:
                %s""".formatted(
                replyBody.stripTrailing(),
                QUOTE_DATE.format(original.receivedAt()),
                original.from(),
                quoted);
    }


}
