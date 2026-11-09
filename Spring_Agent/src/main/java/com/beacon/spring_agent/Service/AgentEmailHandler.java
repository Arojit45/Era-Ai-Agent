package com.beacon.spring_agent.Service;

import com.beacon.spring_agent.dto.AgentResponseDto;
import com.beacon.spring_agent.dto.IncomingEmailDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Primary
public class AgentEmailHandler implements EmailHandler{
    private final SupportAgent_LLM agent;
    private final SupportEmailSender sender;
    @Override
    public boolean handle(IncomingEmailDto email) {
        try {
            AgentResponseDto res = agent.resolve(email);
            return sender.sendReplay(email,res);
        }
        catch (Exception e){
            return false;
        }
    }
}
