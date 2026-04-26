package com.eazybytes.springai.tools;

import com.eazybytes.springai.entity.HelpDeskTicket;
import com.eazybytes.springai.model.TicketRequest;
import com.eazybytes.springai.service.HelpDeskTicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

// New in Sec6_Chap71
@Slf4j
@Component
@RequiredArgsConstructor
public class HelpDeskTools {

    private final HelpDeskTicketService ticketService;

    @Tool(name ="createTicket",description = "Create a support ticket for IT helpdesk",returnDirect = true)
    public String createTicket(@ToolParam(description = "Create Support Ticket") TicketRequest ticketRequest,
                               ToolContext toolCtx) {

        // accessing tool context
        String username = (String)toolCtx.getContext().get("username");

        log.info("### Creating ticket for user: {} with issue: {} ###", username, ticketRequest.issue());
        HelpDeskTicket ticket = ticketService.createTicket(ticketRequest, username);
        log.info("### Ticket created with ID: {} for user: {} ###", ticket.getId(), username);

        return "Ticket created with ID: " + ticket.getId() + " for user: "
                + username + " with issue: " + ticketRequest.issue();
    }

    @Tool(description = "Fetch the status of tickets based on given username")
    public List<HelpDeskTicket> getTicketStatus(ToolContext toolContext) {

        String username = (String)toolContext.getContext().get("username");

        log.info("### Fetching tickets for user: {} ###", username);
        List<HelpDeskTicket> ticketsByUsername = ticketService.getTicketsByUsername(username);
        log.info("### Found tickets {} for user: {} ###", ticketsByUsername, username);

        return ticketsByUsername;

    }

}
