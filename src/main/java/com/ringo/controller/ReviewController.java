package com.ringo.controller;

import com.ringo.dto.ReviewPageRequestDto;
import com.ringo.dto.company.OrganisationResponseDto;
import com.ringo.dto.company.ReviewRequestDto;
import com.ringo.dto.company.ReviewResponseDto;
import com.ringo.service.company.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organisations")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @Operation(summary = "Create a review")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Organisation rated"),
                    @ApiResponse(responseCode = "400", description = "Organisation not found"),
                    @ApiResponse(responseCode = "400", description = "Current user is not a participant")
            }
    )
    @PostMapping(value = "{id}/reviews", produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<OrganisationResponseDto> createReview(
            @PathVariable("id") Long id,
            @RequestBody ReviewRequestDto dto) {
        return ResponseEntity.ok(reviewService.createReview(id, dto));
    }

    @Operation(summary = "Delete a review")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Review deleted"),
                    @ApiResponse(responseCode = "400", description = "Review not found")
            }
    )
    @DeleteMapping(value = "{org_id}/reviews", produces = {"application/json"})
    public ResponseEntity<OrganisationResponseDto> deleteReview(
            @PathVariable("org_id") Long organisationId) {
        return ResponseEntity.ok(reviewService.deleteReview(organisationId));
    }

    @Operation(summary = "Update review")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Review updated"),
                    @ApiResponse(responseCode = "400", description = "Review not found")
            }
    )
    @PutMapping(value = "{org_id}/reviews", produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<OrganisationResponseDto> updateReview(@RequestBody ReviewRequestDto dto,
                                                                @PathVariable("org_id") Long organisationId) {
        return ResponseEntity.ok(reviewService.updateReview(organisationId, dto));
    }

    @Operation(summary = "Find all reviews")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Found all reviews"),
            }
    )
    @GetMapping(value = "{id}/reviews", produces = {"application/json"})
    public ResponseEntity<List<ReviewResponseDto>> findAllReviews(
            @PathVariable("id") Long organisationId,
            ReviewPageRequestDto request) {
        return ResponseEntity.ok(reviewService.findAllByOrganisation(organisationId, request));
    }
}
