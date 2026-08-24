package com.tripnest.tripnest_backend.controller;

import com.tripnest.tripnest_backend.entity.Itinerary;
import com.tripnest.tripnest_backend.service.ItineraryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class ItineraryController {

    private final ItineraryService itineraryService;

    public ItineraryController(
            ItineraryService itineraryService) {

        this.itineraryService = itineraryService;
    }

    // CREATE
    @PostMapping("/{tripId}/itineraries")
    public Itinerary createItinerary(
            @PathVariable Integer tripId,
            @RequestBody Itinerary itinerary) {

        return itineraryService.createItinerary(
                tripId,
                itinerary);
    }

    // GET
    @GetMapping("/{tripId}/itineraries")
    public List<Itinerary> getItineraries(
            @PathVariable Integer tripId) {

        return itineraryService
                .getItinerariesByTrip(tripId);
    }

    // UPDATE
    @PutMapping("/{tripId}/itineraries/{itineraryId}")
    public Itinerary updateItinerary(
            @PathVariable Integer tripId,
            @PathVariable Integer itineraryId,
            @RequestBody Itinerary itinerary) {

        return itineraryService.updateItinerary(
                tripId,
                itineraryId,
                itinerary);
    }

    // DELETE
    @DeleteMapping("/{tripId}/itineraries/{itineraryId}")
    public String deleteItinerary(
            @PathVariable Integer tripId,
            @PathVariable Integer itineraryId) {

        itineraryService.deleteItinerary(
                tripId,
                itineraryId);

        return "Itinerary deleted successfully";
    }
}