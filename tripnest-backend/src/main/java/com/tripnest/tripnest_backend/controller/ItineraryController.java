package com.tripnest.tripnest_backend.controller;

import com.tripnest.tripnest_backend.entity.Itinerary;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.UserRepository;
import com.tripnest.tripnest_backend.service.ItineraryService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@CrossOrigin
public class ItineraryController {

    private final ItineraryService itineraryService;
    private final UserRepository userRepository;

    public ItineraryController(
            ItineraryService itineraryService,
            UserRepository userRepository) {

        this.itineraryService = itineraryService;
        this.userRepository = userRepository;
    }

    // =====================================================
    // CREATE ITINERARY
    // =====================================================

    @PostMapping("/{tripId}/itineraries")
    public Itinerary createItinerary(
            @PathVariable Integer tripId,
            @RequestBody Itinerary itinerary,
            Authentication authentication) {

        User currentUser = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Current user not found"));

        return itineraryService.createItinerary(
                tripId,
                itinerary,
                currentUser
        );
    }

    // =====================================================
    // GET ITINERARIES
    // =====================================================

    @GetMapping("/{tripId}/itineraries")
    public ResponseEntity<List<Itinerary>> getItineraries(
            @PathVariable Integer tripId,
            Authentication authentication) {

        User currentUser = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Current user not found"));

        return ResponseEntity.ok(
                itineraryService.getItinerariesByTrip(
                        tripId,
                        currentUser
                )
        );
    }

    // =====================================================
    // UPDATE ITINERARY
    // =====================================================

    @PutMapping("/{tripId}/itineraries/{itineraryId}")
    public Itinerary updateItinerary(
            @PathVariable Integer tripId,
            @PathVariable Integer itineraryId,
            @RequestBody Itinerary itinerary,
            Authentication authentication) {

        User currentUser = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Current user not found"));

        return itineraryService.updateItinerary(
                tripId,
                itineraryId,
                itinerary,
                currentUser
        );
    }

    // =====================================================
    // DELETE ITINERARY
    // =====================================================

    @DeleteMapping("/{tripId}/itineraries/{itineraryId}")
    public String deleteItinerary(
            @PathVariable Integer tripId,
            @PathVariable Integer itineraryId,
            Authentication authentication) {

        User currentUser = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Current user not found"));

        itineraryService.deleteItinerary(
                tripId,
                itineraryId,
                currentUser
        );

        return "Itinerary deleted successfully";
    }
}