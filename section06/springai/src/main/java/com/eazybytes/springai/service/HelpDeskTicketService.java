package com.eazybytes.springai.service;

import com.eazybytes.springai.entity.HelpDeskTicket;
import com.eazybytes.springai.model.TicketRequest;
import com.eazybytes.springai.repository.HelpDeskTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HelpDeskTicketService {

    private final HelpDeskTicketRepository ticketRepository;

    public HelpDeskTicket createTicket(TicketRequest ticketRequest, String username) {
        HelpDeskTicket ticket = HelpDeskTicket.builder()
                .username(username)
                .issue(ticketRequest.issue())
                .status("OPEN")
                .eta(LocalDateTime.now().plusDays(7)) // Example ETA: 3 days from now
                .createdAt(LocalDateTime.now())
                .build();

        return ticketRepository.save(ticket);
    }

    public List<HelpDeskTicket> getTicketsByUsername(String username) {
        return ticketRepository.findByUsername(username);
    }

}
