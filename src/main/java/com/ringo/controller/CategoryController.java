package com.ringo.controller;

import com.ringo.dto.company.CategoryDto;
import com.ringo.service.company.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Find category by id")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Found the category",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryDto.class))),
                    @ApiResponse(responseCode = "404", description = "Category not found", content = @Content)
            }
    )
    @GetMapping(value = "{id}", produces = {"application/json"})
    public ResponseEntity<CategoryDto> findCategoryById(@Parameter(description = "Category id") @PathVariable("id") Long id) {
        return ResponseEntity
                .ok()
                .body(categoryService.findCategoryById(id));
    }

    @Operation(summary = "Create a new category")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Category created",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
            }
    )
    @PostMapping(produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<CategoryDto> createCategory(@Parameter(description = "Category to create") @RequestBody CategoryDto dto) {
        return ResponseEntity
                .ok()
                .body(categoryService.saveCategory(dto));
    }
}
