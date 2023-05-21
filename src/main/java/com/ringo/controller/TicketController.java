package com.ringo.controller;

import com.ringo.dto.common.TicketCode;
import com.ringo.dto.company.TicketDto;
import com.ringo.service.company.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping(value = "/{id}", produces = {"application/json"})
    public ResponseEntity<List<TicketDto>> findTicketById(@PathVariable("id") Long id) {
        return ResponseEntity.ok()
                .body(ticketService.findByEventId(id));
    }

    @PostMapping(value = "/validate", produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<TicketDto> validateTicket(@RequestBody TicketCode ticketCode) {
        return ResponseEntity.ok()
                .body(ticketService.validateTicket(ticketCode));
    }
}
