package com.example.demo.tools;


import com.example.demo.dto.TicketBookingDto;
import com.example.demo.dto.TicketBookingElicitationInfo;
import com.example.demo.entity.TicketEntity;
import com.example.demo.service.TicketBookingService;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpRequestContextTypes;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.ai.mcp.annotation.context.StructuredElicitResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingTicketTools {
    private final TicketBookingService ticketBookingService;
    private static final String DEFAULT_PRIORITY= "MEDIUM";
    private static final String NO_PHONE_NUMBER = "N/A";

    @McpTool(name = "createTicket",description = "Create the Support Ticket")
    public String createTicket(@McpToolParam(description = "Details To Create Ticket")
                               TicketBookingDto ticketBookingDto,McpSyncRequestContext mcp){
        String priority = DEFAULT_PRIORITY;
        String contact = NO_PHONE_NUMBER;
        if(mcp.elicitEnabled()){
            //Receive the Extra Data You need
            StructuredElicitResult<TicketBookingElicitationInfo> result = mcp.elicit((McpRequestContextTypes.ElicitationSpec spec)->
                    spec.message("Before we " +
                            "open your Tickets please Select Your Priority"+"(LOW,HIGH & URGENT) and share an contact number so we create your ticket"
                            +"So team can reach you after the resolve of Your ticket"),TicketBookingElicitationInfo.class);
            if(result.action()== McpSchema.ElicitResult.Action.ACCEPT && result.structuredContent()!=null){
                TicketBookingElicitationInfo info = result.structuredContent();
                if(info.priority()!=null && !info.priority().isBlank()) priority = info.priority();
                if(info.contact()!=null && !info.contact().isBlank()) contact = info.contact();
            }
            else mcp.info("Elicitation Info is Not Provided and SO Go With the BY Default Values");
        }
        TicketEntity savedTicket =  ticketBookingService.create(ticketBookingDto,contact,priority);
        return "Ticket #" + savedTicket.getId() +" Created SuccessFully For User";
    }
    @McpTool(name = "getAllTickets",description = "Fetch the All the Tickets based on the given UserName")
    public List<TicketEntity> getAllUserTicket(@McpToolParam(description = "Username To Fetch the user detail of the Ticket") String username, McpSyncRequestContext mcpSyncRequestContext) throws InterruptedException {
        for(int i=1;i<=10;i++){
            Thread.sleep(1000);
            int percentage = i*10;
            mcpSyncRequestContext.progress(progressSpec -> progressSpec.progress(percentage));
        }
        mcpSyncRequestContext.info("Req come to Get the All the Tickets of " + username);
        return ticketBookingService.getAllTicket(username);
    }
    @McpTool(name = "summary",description = "Generate the friendly, natural language summary of all the status of the ticket of given username")
    public String summary(@McpToolParam(description = "username to summarized the status of the tickets") String username,McpSyncRequestContext context){
        List<TicketEntity> tickets = ticketBookingService.getAllTicket(username);
        if(tickets.isEmpty()) return "No Tickets is Found for user "+username+" .";
        //check is sampling is Allow or Not by MCP Client
        if(!context.sampleEnabled()){
            return tickets.toString();
        }
        String ticketData = tickets.stream()
                .map(t->t.getId()+" | Issue: "+ t.getIssue()+ " | Status:" + t.getStatus() + " | ETA: "
                        + t.getEct())
                .collect(Collectors.joining("\n"));
        String SystemPrompt = """
                You are a friendly help desk assistant. Using ONLY the ticket data provided by the user. Write
                a short summary for the Customer about the status of there Tickets.Mention how many tickets they have
                in total, group them by status (OPEN, IN_PROGRESS, CLOSED) and reassure about the ones that are still being
                worked on. Keep it under 120 words and do not invent any information that is not present in the tickets data.
                And at last write a short line of regret and sorry that you have to wait till now we solve this in Urgent if user have ticket
                Otherwise do not add this line.
                """;
        context.info("Ask your AI Assistance to Summarize "+ tickets.size()+" tickets for "+ username);
        McpSchema.CreateMessageResult result = context.sample(spec->spec
                .systemPrompt(SystemPrompt).message("Here are the support tickets for "+ username+":\n"+ticketData));
        return ((McpSchema.TextContent) result.content()).text();


    }
}
