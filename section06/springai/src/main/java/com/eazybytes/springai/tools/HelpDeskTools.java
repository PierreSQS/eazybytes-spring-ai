package com.eazybytes.springai.tools;

import com.eazybytes.springai.entity.HelpDeskTicket;
import com.eazybytes.springai.model.TicketRequest;
import com.eazybytes.springai.service.HelpDeskTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HelpDeskTools {

    private final HelpDeskTicketService ticketService;

    @Tool(name ="create Ticket",description = "Create a support ticket for IT helpdesk")
    public String createTicket(@ToolParam(description = "Create Support Ticket") TicketRequest ticketRequest,
                               ToolContext toolCtx) {

        // accessing tool context
        String username = (String)toolCtx.getContext().get("username");

        HelpDeskTicket ticket = ticketService.createTicket(ticketRequest, username);

        return "Ticket created with ID: " + ticket.getId() + " for user: "
                + username + " with issue: " + ticketRequest.issue();
    }

    @Tool(description = "Fetch the status of Open tickets based on given username")
    public List<HelpDeskTicket> getTicketStatus(ToolContext toolContext) {

        String username = (String)toolContext.getContext().get("username");

        return ticketService.getTicketsByUsername(username);

    }

}
