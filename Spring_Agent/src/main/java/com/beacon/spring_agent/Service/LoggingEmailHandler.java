package com.beacon.spring_agent.Service;

import com.beacon.spring_agent.dto.IncomingEmailDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoggingEmailHandler implements EmailHandler{
    @Override
    public boolean handle(IncomingEmailDto email) {
        log.info("""
                === New email ===
                From    : {}
                To      : {}
                Subject : {}
                Body    :
                {}
                =================""",
                email.from(), email.to(), email.subject(), email.body());
        return true;
    }
}
