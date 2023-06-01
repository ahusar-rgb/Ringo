package com.ringo.controller;

import com.ringo.dto.company.OrganisationRequestDto;
import com.ringo.dto.company.OrganisationResponseDto;
import com.ringo.dto.company.ReviewRequestDto;
import com.ringo.dto.company.ReviewResponseDto;
import com.ringo.service.company.OrganisationService;
import com.ringo.service.company.ReviewService;
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
    private final ReviewService reviewService;

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

    @Operation(summary = "Rate an organisation")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Organisation rated"),
                    @ApiResponse(responseCode = "400", description = "Organisation not found"),
                    @ApiResponse(responseCode = "400", description = "Current user is not a prticipant")
            }
    )
    @PostMapping(value = "/rate", produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<ReviewResponseDto> rateOrganisation(@RequestBody ReviewRequestDto dto) {
        return ResponseEntity.ok(reviewService.createReview(dto));
    }

    @Operation(summary = "Delete a review")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Review deleted"),
                    @ApiResponse(responseCode = "400", description = "Review not found")
            }
    )
    @DeleteMapping(value = "/rate/{id}", produces = {"application/json"})
    public ResponseEntity<ReviewResponseDto> deleteReview(@PathVariable("id") Long id) {
        return ResponseEntity.ok(reviewService.deleteReview(id));
    }

    @Operation(summary = "Update review")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Review updated"),
                    @ApiResponse(responseCode = "400", description = "Review not found")
            }
    )
    @PutMapping(value = "/rate", produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<ReviewResponseDto> updateReview(@RequestBody ReviewRequestDto dto) {
        return ResponseEntity.ok(reviewService.updateReview(dto));
    }
}
