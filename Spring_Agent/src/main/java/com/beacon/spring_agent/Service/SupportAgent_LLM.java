package com.beacon.spring_agent.Service;

import com.beacon.spring_agent.Config.InboxProperties;
import com.beacon.spring_agent.dto.AgentResponseDto;
import com.beacon.spring_agent.dto.IncomingEmailDto;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class SupportAgent_LLM {
    private final ChatClient client;
    public SupportAgent_LLM(ChatClient.Builder chatClient,ToolCallbackProvider toolCallbackProvider, InboxProperties props,@Value("classpath:/Prompt/EmailSystemPrompt.st") Resource SystemPrompt) {
        this.client = chatClient
                .defaultSystem(sys->sys.text(SystemPrompt).param("support_email",props.address()))
                .defaultTools(toolCallbackProvider)
                .build();
    }

    public AgentResponseDto resolve(IncomingEmailDto email){
        return client.prompt()
                .user(u -> u.text("""
                        A new email just arrived in the support inbox. Resolve it.

                        From       : {from}
                        To         : {to}
                        Received   : {receivedAt}
                        Subject    : {subject}

                        Body:
                        {body}
                        """)
                        .param("from", email.from())
                        .param("to", String.join(", ", email.to()))
                        .param("receivedAt", email.receivedAt())
                        .param("subject", email.subject())
                        .param("body", email.body()))
                .call().entity(AgentResponseDto.class);
    }
}
