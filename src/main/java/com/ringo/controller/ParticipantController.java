package com.ringo.controller;

import com.ringo.dto.company.request.ParticipantRequestDto;
import com.ringo.dto.company.response.ParticipantResponseDto;
import com.ringo.dto.security.IdTokenDto;
import com.ringo.service.company.ParticipantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/participants")
@Slf4j
@Validated
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
    public ResponseEntity<ParticipantResponseDto> createParticipant(@Valid @RequestBody ParticipantRequestDto dto) {
        return ResponseEntity.ok(participantService.save(dto));
    }

    @PutMapping(produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<ParticipantResponseDto> updateParticipant(@Valid @RequestBody ParticipantRequestDto dto) {
        return ResponseEntity.ok(participantService.partialUpdate(dto));
    }

    @PutMapping(value = "/profile-picture", produces = {"application/json"}, consumes = {"multipart/form-data"})
    public ResponseEntity<ParticipantResponseDto> updateProfilePicture(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(participantService.setPhoto(file));
    }

    @PutMapping(value = "/profile-picture/remove", produces = {"application/json"})
    public ResponseEntity<ParticipantResponseDto> removeProfilePicture() {
        return ResponseEntity.ok(participantService.removePhoto());
    }

    @PostMapping(value = "/sign-up/google", produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<ParticipantResponseDto> signUpGoogle(@Valid @RequestBody IdTokenDto token) {
        return ResponseEntity.ok(participantService.signUpGoogle(token.getIdToken()));
    }

    @PostMapping(value = "/sign-up/apple", produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<ParticipantResponseDto> signInApple(@Valid @RequestBody IdTokenDto token) {
        return ResponseEntity.ok(participantService.signUpApple(token.getIdToken()));
    }

    @PostMapping(value = "activate", produces = {"application/json"})
    public ResponseEntity<ParticipantResponseDto> activateParticipant() {
        return ResponseEntity.ok(participantService.activate());
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteParticipant() {
        participantService.delete();
        return ResponseEntity.ok().build();
    }
}
