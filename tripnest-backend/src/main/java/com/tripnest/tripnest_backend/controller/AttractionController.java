package com.tripnest.tripnest_backend.controller;

import com.tripnest.tripnest_backend.dto.AttractionRequest;
import com.tripnest.tripnest_backend.dto.AttractionResponse;
import com.tripnest.tripnest_backend.service.AttractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/destinations/{destinationId}/attractions")
@CrossOrigin
@RequiredArgsConstructor
public class AttractionController {

    private final AttractionService attractionService;

    // =====================================================
    // 1. LIST ATTRACTIONS FOR DESTINATION
    // Accessible to all authenticated users
    // =====================================================
    @GetMapping
    public ResponseEntity<?> getAttractionsByDestination(@PathVariable Integer destinationId) {
        try {
            List<AttractionResponse> attractions = attractionService.getAttractionsByDestination(destinationId);
            return ResponseEntity.ok(attractions);
        } catch (RuntimeException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("not found")) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", ex.getMessage()));
            }
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    // =====================================================
    // 2. ADMIN CREATE ATTRACTION FOR DESTINATION
    // Restricted strictly to ADMINISTRATOR / ADMIN users
    // =====================================================
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'ADMIN')")
    public ResponseEntity<?> createAttraction(
            @PathVariable Integer destinationId,
            @Valid @RequestBody AttractionRequest request) {

        if (request == null || request.getName() == null || request.getName().trim().isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Attraction name must not be blank"));
        }

        try {
            AttractionResponse created = attractionService.createAttraction(destinationId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        } catch (RuntimeException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("not found")) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", ex.getMessage()));
            }
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        }
    }
}
