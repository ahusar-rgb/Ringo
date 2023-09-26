package com.ringo.controller;

import com.ringo.dto.company.request.OrganisationRequestDto;
import com.ringo.dto.company.response.OrganisationResponseDto;
import com.ringo.dto.security.IdTokenDto;
import com.ringo.service.company.OrganisationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/organisations")
@RequiredArgsConstructor
@Validated
public class OrganisationController {

    private final OrganisationService organisationService;

    @Operation(summary = "Find organisation by id")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Found the organisation"),
                    @ApiResponse(responseCode = "404", description = "Organisation not found")
            }
    )
    @GetMapping(value = "/{id}", produces = {"application/json"})
    public ResponseEntity<OrganisationResponseDto> findOrganisationById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(organisationService.findById(id));
    }

    @Operation(summary = "Find current organisation")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Found the organisation"),
                    @ApiResponse(responseCode = "404", description = "Organisation not found")
            }
    )
    @GetMapping(produces = {"application/json"})
    public ResponseEntity<OrganisationResponseDto> findCurrentOrganisation() {
        return ResponseEntity.ok(organisationService.findCurrentOrganisation());
    }

    @Operation(summary = "Create a new organisation")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Organisation created"),
                    @ApiResponse(responseCode = "400", description = "Invalid arguments")
            }
    )
    @PostMapping(value = "/sign-up", produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<OrganisationResponseDto> createOrganisation(@Valid @RequestBody OrganisationRequestDto dto) {
        return ResponseEntity.ok(organisationService.save(dto));
    }

    @Operation(summary = "Update an existing organisation")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Organisation updated"),
                    @ApiResponse(responseCode = "404", description = "Organisation not found")
            }
    )
    @PutMapping(produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<OrganisationResponseDto> updateOrganisation(@Valid @RequestBody OrganisationRequestDto dto) {
        return ResponseEntity.ok(organisationService.partialUpdate(dto));
    }

    @PutMapping(value = "/profile-picture", produces = {"application/json"}, consumes = {"multipart/form-data"})
    public ResponseEntity<OrganisationResponseDto> updateProfilePicture(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(organisationService.setPhoto(file));
    }

    @PutMapping(value = "/profile-picture/remove", produces = {"application/json"})
    public ResponseEntity<OrganisationResponseDto> removeProfilePicture() {
        return ResponseEntity.ok(organisationService.removePhoto());
    }

    @PostMapping("/activate")
    public ResponseEntity<OrganisationResponseDto> activateOrganisation() {
        return ResponseEntity.ok(organisationService.activate());
    }

    @PostMapping(value = "sign-up/google", consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<OrganisationResponseDto> signUpGoogle(@Valid @RequestBody IdTokenDto token) {
        return ResponseEntity.ok(organisationService.signUpGoogle(token.getIdToken()));
    }

    @PostMapping(value = "sign-up/apple", consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<OrganisationResponseDto> signInApple(@Valid @RequestBody IdTokenDto token) {
        return ResponseEntity.ok(organisationService.signUpApple(token.getIdToken()));
    }

    @GetMapping(value = "account-link", produces = {"application/json"})
    public ResponseEntity<String> getAccountLink() {
        return ResponseEntity.ok(organisationService.getAccountLink());
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteOrganisation() {
        organisationService.delete();
        return ResponseEntity.ok().build();
    }
}
