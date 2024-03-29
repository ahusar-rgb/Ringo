package com.ringo.controller;

import com.ringo.dto.common.TicketCode;
import com.ringo.dto.company.response.TicketDto;
import com.ringo.service.company.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Validated
public class TicketController {
    private final TicketService ticketService;

    @Operation(summary = "Scan ticket by code (encoded in QR)")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketDto.class))),
                    @ApiResponse(responseCode = "404", description = "Ticket not found", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Event not found", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Participant not found", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Ticket expired", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Current user is not the host of this event", content = @Content)
            }
    )
    @PostMapping(value = "/scan", produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<TicketDto> scanTicket(@Valid @RequestBody TicketCode ticketCode) {
        return ResponseEntity.ok()
                .body(ticketService.scanTicket(ticketCode));
    }

    @Operation(summary = "Find tickets by event id")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = TicketDto.class)))),
                    @ApiResponse(responseCode = "400", description = "Event not found", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Current user is not the host of this event", content = @Content)
            }
    )
    @GetMapping(value = "/{id}", produces = {"application/json"})
    public ResponseEntity<List<TicketDto>> findTicketsByEventId(@PathVariable("id") Long id) {
        return ResponseEntity.ok()
                .body(ticketService.findByEventId(id));
    }


    @Operation(summary = "Validate ticket by code (encoded in QR)")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketDto.class))),
                    @ApiResponse(responseCode = "404", description = "Ticket not found", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Event not found", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Participant not found", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Ticket expired", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Current user is not the host of this event", content = @Content)
            }
    )
    @PostMapping(value = "/validate", produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<Void> validateTicket(@Valid @RequestBody TicketCode ticketCode) {
        ticketService.validateTicket(ticketCode);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get acquired tickets for current participant")
    @ApiResponses(
            value = {
                 @ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = TicketDto.class)))),
                    @ApiResponse(responseCode = "400", description = "Event not found", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Current user is not a participant", content = @Content)
            }
    )
    @GetMapping(produces = {"application/json"})
    public ResponseEntity<List<TicketDto>> getMyTickets() {
        return ResponseEntity.ok()
                .body(ticketService.getMyTickets());
    }

    @PostMapping(value = "/issue/{event_id}/ticket-type/{ticket_type_id}/{email}", produces = {"application/json"})
    public ResponseEntity<String> issueTicketByEmail(
            @PathVariable("event_id") Long eventId,
            @PathVariable("ticket_type_id") Long ticketTypeId,
            @PathVariable("email") String email) {
        ticketService.issueToUserByEmail(eventId, ticketTypeId, email);
        return ResponseEntity.ok("Ticket issued successfully");
    }
}
