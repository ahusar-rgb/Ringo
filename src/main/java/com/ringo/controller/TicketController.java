package com.ringo.controller;

import com.ringo.dto.common.TicketCode;
import com.ringo.dto.company.TicketDto;
import com.ringo.service.company.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;

    @PostMapping(value = "/scan", produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<TicketDto> scanTicket(@RequestBody TicketCode ticketCode) {
        return ResponseEntity.ok()
                .body(ticketService.scanTicket(ticketCode));
    }
}
