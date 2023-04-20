package com.ringo.controller;

import com.ringo.dto.company.*;
import com.ringo.dto.search.EventSearchDto;
import com.ringo.service.company.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventService eventService;

    @Operation(summary = "Find event by id")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Found the event",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Event not found", content = @Content)
            }
    )
    @GetMapping(value = "/{id}", produces = {"application/json"})
    public ResponseEntity<EventResponseDto> findEventById(@Parameter(description = "Event id") @PathVariable Long id) {
        return ResponseEntity
                .ok()
                .body(eventService.findEventById(id));
    }

    @Operation(summary = "Find all events near the given coordinates")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Found the events",
                        content = @Content(mediaType = "application/json",
                                array = @ArraySchema(schema = @Schema(implementation = EventSmallDto.class))))
            }
    )
    @GetMapping(value = "geo/near", produces = {"application/json"})
    public ResponseEntity<List<EventSmallDto>> findEventsByDistance(
            @Parameter(description = "latitude") @RequestParam Double lat,
            @Parameter(description = "longitude") @RequestParam Double lon,
            @Parameter(description = "distance") @RequestParam Integer limit) {
        return ResponseEntity.ok()
                .body(eventService.findTopByDistance(lat, lon, limit));
    }

    @Operation(summary = "Find all events in the given area")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Found the events",
                        content = @Content(mediaType = "application/json",
                                array = @ArraySchema(schema = @Schema(implementation = EventGroupDto.class))))
            }
    )
    @GetMapping(value = "geo/area", produces = {"application/json"})
    public ResponseEntity<List<EventGroupDto>> findEventsInArea(
            @Parameter(description = "min latitude") @RequestParam Double latMin,
            @Parameter(description = "max latitude") @RequestParam Double latMax,
            @Parameter(description = "min longitude") @RequestParam Double lonMin,
            @Parameter(description = "max longitude") @RequestParam Double lonMax) {
        return ResponseEntity.ok()
                .body(eventService.findEventsInArea(latMin, latMax, lonMin, lonMax));
    }

    @Operation(summary = "Create a new event")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Event created",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponseDto.class))),
            }
    )
    @PostMapping(produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<EventResponseDto> saveEvent(
            @Parameter(description = "Event to save") @RequestBody EventRequestDto eventDto
    ) {
        return ResponseEntity
                .ok()
                .body(eventService.saveEvent(eventDto));
    }

    @Operation(summary = "Add photo to event")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Photo added to event",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "404", description = "Event not found", content = @Content)
            }
    )
    @PutMapping(value = "/add-photo", produces = {"application/json"}, consumes = {"multipart/form-data"})
    public ResponseEntity<String> addPhotoToEvent(
            @Parameter(description = "Data about the request") @RequestPart("data") AddEventPhotoRequest request,
            @Parameter(description = "photo") @RequestPart("file") MultipartFile photo) {

        eventService.addPhotoToEvent(request, photo);
        return ResponseEntity
                .ok("Success");
    }

    @Operation(summary = "Search events")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Found the events",
                        content = @Content(mediaType = "application/json",
                                array = @ArraySchema(schema = @Schema(implementation = EventSmallDto.class))))
            }
    )
    @GetMapping(produces = {"application/json"})
    public ResponseEntity<List<EventSmallDto>> searchEvent(EventSearchDto searchDto) {
        return ResponseEntity.ok()
                .body(eventService.searchEvents(searchDto));
    }

}
