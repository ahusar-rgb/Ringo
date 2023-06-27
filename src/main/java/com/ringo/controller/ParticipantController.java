package com.ringo.controller;

import com.ringo.dto.company.ParticipantRequestDto;
import com.ringo.dto.company.ParticipantResponseDto;
import com.ringo.dto.security.IdTokenDto;
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
        return ResponseEntity.ok(participantService.findById(id));
    }

    @GetMapping(produces = {"application/json"})
    public ResponseEntity<ParticipantResponseDto> findCurrentParticipant() {
        return ResponseEntity.ok(participantService.findCurrentParticipant());
    }

    @PostMapping(value = "/sign-up", produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<ParticipantResponseDto> createParticipant(@RequestBody ParticipantRequestDto dto) {
        return ResponseEntity.ok(participantService.save(dto));
    }

    @PutMapping(produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<ParticipantResponseDto> updateParticipant(@RequestBody ParticipantRequestDto dto) {
        return ResponseEntity.ok(participantService.update(dto));
    }

    @PostMapping(value = "/sign-up/google", produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<ParticipantResponseDto> signUpGoogle(@RequestBody IdTokenDto token) {
        return ResponseEntity.ok(participantService.signUpGoogle(token.getIdToken()));
    }

    @PostMapping(value = "/sign-up/apple", produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<ParticipantResponseDto> signInApple(@RequestBody IdTokenDto token) {
        return ResponseEntity.ok(participantService.signUpApple(token.getIdToken()));
    }

    @PostMapping(value = "activate", produces = {"application/json"})
    public ResponseEntity<ParticipantResponseDto> activateParticipant() {
        return ResponseEntity.ok(participantService.activate());
    }
}
