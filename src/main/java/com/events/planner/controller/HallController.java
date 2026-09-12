/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.events.planner.controller;

import com.events.planner.dto.HallDto;
import com.events.planner.service.HallService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author MAU
 */
@Tag(name = "Halls", description = "CRUD operations for halls")
@SecurityRequirement(name = "basicAuth")
@RestController
@RequestMapping("/api/halls")
public class HallController {

    private final HallService hallService;

    public HallController(HallService hallService) {
        this.hallService = hallService;
    }

    @Operation(summary = "Create hall", description = "Creates a new hall.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created",
                content = @Content(schema = @Schema(implementation = HallDto.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
                content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<HallDto> create(@RequestBody HallDto dto) throws Exception {
        return new ResponseEntity<>(hallService.create(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Get hall by id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
                content = @Content(schema = @Schema(implementation = HallDto.class))),
        @ApiResponse(responseCode = "400", description = "Hall not found",
                content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<HallDto> getById(
            @Parameter(description = "Hall id", example = "1")
            @PathVariable Long id) throws Exception {
        return ResponseEntity.ok(hallService.getById(id));
    }

    @Operation(summary = "Get all halls (paged)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping
    public ResponseEntity<Page<HallDto>> getAll(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 50)", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(hallService.getAll(page, size));
    }

    @Operation(summary = "Search halls by name")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<HallDto>> searchByName(
            @Parameter(description = "Hall name", example = "Main")
            @RequestParam String name,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 50)", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(hallService.searchByName(name, page, size));
    }

    @Operation(summary = "Get halls by type")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping("/type")
    public ResponseEntity<Page<HallDto>> getByType(
            @Parameter(description = "Hall type", example = "Conference")
            @RequestParam String type,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 50)", example = "10")
            @RequestParam(defaultValue = "10") int size) throws Exception {
        return ResponseEntity.ok(hallService.getByType(type, page, size));
    }

    @Operation(summary = "Get halls by minimum capacity")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping("/capacity")
    public ResponseEntity<Page<HallDto>> getByMinCapacity(
            @Parameter(description = "Minimum capacity", example = "50")
            @RequestParam int minCapacity,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 50)", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(hallService.getByMinCapacity(minCapacity, page, size));
    }

    @Operation(summary = "Update hall", description = "Updates an existing hall by id.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
                content = @Content(schema = @Schema(implementation = HallDto.class))),
        @ApiResponse(responseCode = "400", description = "Hall not found / validation error",
                content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<HallDto> update(
            @Parameter(description = "Hall id", example = "1")
            @PathVariable Long id,
            @RequestBody HallDto dto) throws Exception {
        return ResponseEntity.ok(hallService.update(id, dto));
    }

    @Operation(summary = "Delete hall")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Deleted"),
        @ApiResponse(responseCode = "400", description = "Hall not found",
                content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Hall id", example = "1")
            @PathVariable Long id) throws Exception {
        hallService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get available halls for an event and time period")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Available halls returned"
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid time period or event not found",
                content = @Content(
                        schema = @Schema(implementation = String.class)
                )
        )
    })
    @GetMapping("/available")
    public List<HallDto> getAvailableHalls(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam Long eventId
    ) throws Exception {

        return hallService.getAvailableHalls(
                start,
                end,
                eventId
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
