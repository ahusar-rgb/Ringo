package com.ringo.controller;

import com.ringo.dto.company.ParticipantRequestDto;
import com.ringo.dto.company.ParticipantResponseDto;
import com.ringo.service.company.ParticipantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/participants")
@Slf4j
public class ParticipantController {
    private final ParticipantService participantService;

    @GetMapping(value = "/{id}", produces = {"application/json"})
    public ResponseEntity<ParticipantResponseDto> findParticipantById(@PathVariable("id") Long id) {
        return ResponseEntity
                .ok()
                .body(participantService.findById(id));
    }

    @GetMapping(produces = {"application/json"})
    public ResponseEntity<ParticipantResponseDto> findCurrentParticipant() {
        return ResponseEntity
                .ok()
                .body(participantService.findCurrentParticipant());
    }

    @PostMapping(value = "/sign-up", produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<String> createParticipant(@RequestBody ParticipantRequestDto dto) {
        ParticipantResponseDto participant = participantService.save(dto);
        return ResponseEntity.ok("Participant created successfully [id: %d]".formatted(participant.getId()));
    }

    @PutMapping(produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<String> updateParticipant(@RequestBody ParticipantRequestDto dto) {
        participantService.update(dto);
        return ResponseEntity.ok("Participant updated successfully");
    }
}
