package com.beacon.spring_agent.Service;

import com.beacon.spring_agent.dto.IncomingEmailDto;

@FunctionalInterface
public interface EmailHandler {
    boolean handle(IncomingEmailDto email);
}
