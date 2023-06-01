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
                    @ApiResponse(responseCode = "200", description = "Event found",
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
    public ResponseEntity<EventResponseDto> createEvent(
            @Parameter(description = "Event to save") @RequestBody EventRequestDto eventDto
    ) {
        return ResponseEntity.ok(eventService.create(eventDto));
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
                .body("Event#%d deleted".formatted(id));
    }

    @Operation(summary = "Update event")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Event updated",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Event not found", content = @Content)
            }
    )
    @PutMapping(value = "/{id}", produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<EventResponseDto> updateEvent(
            @Parameter(description = "Event id") @PathVariable("id") Long id,
            @Parameter(description = "Event to update") @RequestBody EventRequestDto eventDto
    ) {
        return ResponseEntity.ok(eventService.update(id, eventDto));
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
    public ResponseEntity<EventResponseDto> addPhotoToEvent(
            @Parameter(description = "Id of the event") @PathVariable("id") Long id,
            @Parameter(description = "Photo") @RequestPart("file") MultipartFile photo) {

        return ResponseEntity.ok(eventService.addPhoto(id, photo));
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
    @PutMapping(value = "{id}/remove-photo/{photo_id}", produces = {"application/json"})
    public ResponseEntity<EventResponseDto> removePhotoFromEvent(
            @Parameter(description = "Id of the event") @PathVariable("id") Long id,
            @Parameter(description = "Id of the photo") @PathVariable("photo_id") Long photoId) {

        return ResponseEntity.ok(eventService.removePhoto(id, photoId));
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
    @PutMapping(value = "{id}/change-main-photo/{photo_id}", produces = {"application/json"})
    public ResponseEntity<EventResponseDto> changeMainPhoto(@Parameter(description = "Event id") @PathVariable("id") Long id,
                                @Parameter(description = "Photo id") @PathVariable("photo_id") Long photoId) {

        return ResponseEntity.ok(eventService.setMainPhoto(id, photoId));
    }

    @Operation(summary = "Activate event")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Event activated",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Event not found", content = @Content)
            }
    )
    @PutMapping(value = "/{id}/activate", produces = {"application/json"})
    public ResponseEntity<EventResponseDto> activateEvent(@Parameter(description = "Event id") @PathVariable("id") Long id) {
        return ResponseEntity.ok(eventService.activate(id));
    }

    @Operation(summary = "Deactivate event")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Event deactivated",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Event not found", content = @Content)
            }
    )
    @PutMapping(value = "/{id}/deactivate", produces = {"application/json"})
    public ResponseEntity<EventResponseDto> deactivateEvent(@Parameter(description = "Event id") @PathVariable("id") Long id) {
        return ResponseEntity.ok(eventService.deactivate(id));
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

    @Operation(summary = "Join event")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Joined the event",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketDto.class))),
                    @ApiResponse(responseCode = "404", description = "Event not found", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Event is full", content = @Content),
                    @ApiResponse(responseCode = "400", description = "User already joined the event", content = @Content),
                    @ApiResponse(responseCode = "400", description = "User is not a participant", content = @Content)
            }
    )
    @PostMapping(value = "/{id}/join", produces = {"application/json"})
    public ResponseEntity<TicketDto> joinEvent(
            @Parameter(description = "Event id") @PathVariable("id") Long id) {
        return ResponseEntity.ok(eventService.joinEvent(id));
    }

    @Operation(summary = "Leave event")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Left the event",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketDto.class))),
                    @ApiResponse(responseCode = "404", description = "Event not found", content = @Content),
                    @ApiResponse(responseCode = "400", description = "User is not a participant", content = @Content),
                    @ApiResponse(responseCode = "400", description = "The participant is not registered for this event", content = @Content)
            }
    )
    @PostMapping(value = "/{id}/leave", produces = {"application/json"})
    public ResponseEntity<TicketDto> leaveEvent(
            @Parameter(description = "Event id") @PathVariable("id") Long id) {
        return ResponseEntity.ok(eventService.leaveEvent(id));
    }

    @Operation(summary = "Save event")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Event saved",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Event not found", content = @Content),
                    @ApiResponse(responseCode = "400", description = "User is not a participant", content = @Content)
            }
    )
    @PostMapping(value = "/{id}/save", produces = {"application/json"})
    public ResponseEntity<EventResponseDto> saveEvent(
            @Parameter(description = "Event id") @PathVariable("id") Long id) {
        return ResponseEntity.ok(eventService.saveEvent(id));
    }

    @Operation(summary = "Unsave event")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Event unsaved",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Event not found", content = @Content),
                    @ApiResponse(responseCode = "400", description = "User is not a participant", content = @Content)
            }
    )
    @PostMapping(value = "/{id}/unsave", produces = {"application/json"})
    public ResponseEntity<EventResponseDto> unsaveEvent(
            @Parameter(description = "Event id") @PathVariable("id") Long id) {
        return ResponseEntity.ok(eventService.unsaveEvent(id));
    }

    @Operation(summary = "Get saved events")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Found the events",
                            content = @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = EventSmallDto.class))))
            }
    )
    @GetMapping(value = "/saved", produces = {"application/json"})
    public ResponseEntity<List<EventSmallDto>> getSavedEvents() {
        return ResponseEntity.ok()
                .body(eventService.getSavedEvents());
    }
}
