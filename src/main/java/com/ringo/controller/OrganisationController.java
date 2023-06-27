package com.ringo.controller;

import com.ringo.dto.company.OrganisationRequestDto;
import com.ringo.dto.company.OrganisationResponseDto;
import com.ringo.dto.security.IdTokenDto;
import com.ringo.service.company.OrganisationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organisations")
@RequiredArgsConstructor
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
    public ResponseEntity<OrganisationResponseDto> createOrganisation(@RequestBody OrganisationRequestDto dto) {
        return ResponseEntity.ok(organisationService.create(dto));
    }

    @Operation(summary = "Update an existing organisation")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Organisation updated"),
                    @ApiResponse(responseCode = "404", description = "Organisation not found")
            }
    )
    @PutMapping(produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<OrganisationResponseDto> updateOrganisation(@RequestBody OrganisationRequestDto dto) {
        return ResponseEntity.ok(organisationService.update(dto));
    }

    @PostMapping("/activate")
    public ResponseEntity<OrganisationResponseDto> activateOrganisation() {
        return ResponseEntity.ok(organisationService.activate());
    }

    @PostMapping(value = "sign-up/google", consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<OrganisationResponseDto> signUpGoogle(@RequestBody IdTokenDto token) {
        return ResponseEntity.ok(organisationService.signUpGoogle(token.getIdToken()));
    }

    @PostMapping(value = "sign-in/apple", consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<OrganisationResponseDto> signInApple(@RequestBody IdTokenDto token) {
        return ResponseEntity.ok(organisationService.signUpApple(token.getIdToken()));
    }
}
