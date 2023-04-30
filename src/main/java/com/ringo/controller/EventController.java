package com.ringo.controller;

import com.ringo.dto.company.EventGroupDto;
import com.ringo.dto.company.EventRequestDto;
import com.ringo.dto.company.EventResponseDto;
import com.ringo.dto.company.EventSmallDto;
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
                .body(eventService.findById(id));
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
    public ResponseEntity<Long> saveEvent(
            @Parameter(description = "Event to save") @RequestBody EventRequestDto eventDto
    ) {
        return ResponseEntity
                .ok()
                .body(eventService.save(eventDto));
    }

    @Operation(summary = "Delete event by id")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Event deleted",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "404", description = "Event not found", content = @Content)
            }
    )
    @DeleteMapping(value = "/{id}", produces = {"application/json"})
    public ResponseEntity<String> deleteEventById(@Parameter(description = "Event id") @PathVariable("id") Long id) {
        eventService.delete(id);
        return ResponseEntity
                .ok()
                .body("Event#%s deleted".formatted(id));
    }

    @Operation(summary = "Add photo to event")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Photo added to event",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "404", description = "Event not found", content = @Content)
            }
    )
    @PutMapping(value = "/{id}/add-photo", produces = {"application/json"}, consumes = {"multipart/form-data"})
    public ResponseEntity<String> addPhotoToEvent(
            @Parameter(description = "Id of the event") @PathVariable("id") Long id,
            @Parameter(description = "Photo") @RequestPart("file") MultipartFile photo) {

        eventService.addPhoto(id, photo);
        return ResponseEntity
                .ok("Photo %s added to event %s".formatted(photo.getOriginalFilename(), id));
    }

    @Operation(summary = "Remove photo from event")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Photo removed from event",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "404", description = "Event not found", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Photo not found", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Photo not owned by the event", content = @Content)
            }
    )
    @PutMapping(value = "{id}/remove-photo", produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<String> removePhotoFromEvent(
            @Parameter(description = "Id of the event") @PathVariable("id") Long id,
            @Parameter(description = "Id of the photo") Long photoId) {

        eventService.removePhoto(id, photoId);
        return ResponseEntity
                .ok("Photo %s removed from event %s".formatted(photoId, id));
    }

    @Operation(summary = "Set main photo of event")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Main photo changed",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "404", description = "Event not found", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Photo not found", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Photo not owned by the event", content = @Content)
            }
    )
    @PutMapping(value = "{id}/change-main-photo", produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<String> changeMainPhoto(@Parameter(description = "Event id") @PathVariable("id") Long id,
                                @Parameter(description = "Photo id") @RequestBody Long photoId) {
        eventService.setMainPhoto(id, photoId);
        return ResponseEntity
                .ok("Main photo of event %s changed to %s".formatted(id, photoId));
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
                .body(eventService.search(searchDto));
    }

}
