package com.ringo.controller;

import com.ringo.dto.company.OrganisationRequestDto;
import com.ringo.dto.company.OrganisationResponseDto;
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
        return ResponseEntity
                .ok()
                .body(organisationService.findOrganisationById(id));
    }

    @Operation(summary = "Create a new organisation")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Organisation created"),
                    @ApiResponse(responseCode = "400", description = "Invalid input")
            }
    )
    @PostMapping(produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<OrganisationResponseDto> createOrganisation(@RequestBody OrganisationRequestDto dto) {
        return ResponseEntity
                .ok()
                .body(organisationService.createOrganisation(dto));
    }
}
