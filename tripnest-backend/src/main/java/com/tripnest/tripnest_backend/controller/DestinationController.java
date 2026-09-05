package com.tripnest.tripnest_backend.controller;

import com.tripnest.tripnest_backend.entity.Destination;
import com.tripnest.tripnest_backend.service.DestinationService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/destinations")
public class DestinationController {

    private final DestinationService destinationService;

    public DestinationController(DestinationService destinationService) {
        this.destinationService = destinationService;
    }

    // ==========================================
    // GET ALL DESTINATIONS
    // ==========================================

    @GetMapping
    public ResponseEntity<List<Destination>> getAllDestinations() {

        return ResponseEntity.ok(
                destinationService.getAllDestinations()
        );
    }

    // ==========================================
    // GET POPULAR DESTINATIONS
    // ==========================================

    @GetMapping("/popular")
    public ResponseEntity<List<Destination>> getPopularDestinations() {

        return ResponseEntity.ok(
                destinationService.getAllDestinations()
        );
    }

    // ==========================================
    // GOOGLE PLACES SEARCH
    // ==========================================

    @GetMapping("/search")
    public ResponseEntity<?> searchDestinations(
            @RequestParam String query) {

        if (query == null || query.trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Destination search query is required");
        }

        return ResponseEntity.ok(
                destinationService.searchGooglePlaces(
                        query.trim()
                )
        );
    }

    // ==========================================
    // GOOGLE PLACE DETAILS
    // ==========================================

    @GetMapping("/place-details")
    public ResponseEntity<?> getPlaceDetails(
            @RequestParam String placeId) {

        if (placeId == null || placeId.trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Place ID is required");
        }

        return ResponseEntity.ok(
                destinationService.getGooglePlaceDetails(
                        placeId.trim()
                )
        );
    }

    // ==========================================
    // CREATE OR GET DESTINATION
    // ==========================================

    @PostMapping("/create-or-get")
    public ResponseEntity<Destination> createOrGetDestination(
            @RequestBody Map<String, String> request) {

        String name = request.get("name");

        if (name == null || name.trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .build();
        }

        Destination destination =
                destinationService.createOrGetDestination(name);

        return ResponseEntity.ok(destination);
    }

    // ==========================================
    // GET DATABASE DESTINATION BY ID
    // ==========================================

    @GetMapping("/{id}")
    public ResponseEntity<Destination> getDestinationById(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                destinationService.getDestinationById(id)
        );
    }

    @GetMapping("/photo")
public ResponseEntity<byte[]> getPlacePhoto(
        @RequestParam String photoName) {

    return destinationService.getGooglePlacePhoto(photoName);
}
}