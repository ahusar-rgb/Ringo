package com.ringo.controller;

import com.ringo.dto.company.CurrencyDto;
import com.ringo.service.company.CurrencyService;
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

import java.util.List;

@RestController
@RequestMapping("/api/currencies")
@RequiredArgsConstructor
public class CurrencyController {
    private final CurrencyService currencyService;

    @Operation(summary = "Find currency by id")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Found the currency",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CurrencyDto.class))),
                    @ApiResponse(responseCode = "404", description = "Currency not found", content = @Content)
            }
    )
    @GetMapping(value = "{id}", produces = {"application/json"})
    public ResponseEntity<CurrencyDto> findCurrencyById(@Parameter(description = "Currency id") @PathVariable("id") Long id) {
        return ResponseEntity
                .ok()
                .body(currencyService.findCurrencyById(id));
    }

    @Operation(summary = "Create a new currency")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Currency created",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CurrencyDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
            }
    )
    @PostMapping(produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<CurrencyDto> createCurrency(@Parameter(description = "Currency to create") @RequestBody CurrencyDto dto) {
        return ResponseEntity
                .ok()
                .body(currencyService.saveCurrency(dto));
    }

    @Operation(summary = "Get all currencies")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = CurrencyDto.class))))
            }
    )
    @GetMapping(produces = {"application/json"})
    public ResponseEntity<List<CurrencyDto>> listAll() {
        return ResponseEntity
                .ok()
                .body(currencyService.findAll());
    }
}
