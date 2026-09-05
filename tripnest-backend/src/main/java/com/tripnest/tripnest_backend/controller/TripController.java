package com.tripnest.tripnest_backend.controller;

import com.tripnest.tripnest_backend.entity.Trip;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.UserRepository;
import com.tripnest.tripnest_backend.service.TripService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripService tripService;
    private final UserRepository userRepository;

    public TripController(
            TripService tripService,
            UserRepository userRepository) {

        this.tripService = tripService;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(
            Authentication authentication) {

        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    // =====================================================
    // CREATE TRIP
    // =====================================================

    @PostMapping
    public Trip createTrip(
            @RequestBody Trip trip,
            Authentication authentication) {

        User user = getCurrentUser(authentication);

        trip.setUser(user);

        return tripService.createTrip(trip);
    }

    // =====================================================
    // MY TRIPS
    // =====================================================

    @GetMapping({"", "/my"})
    public List<Trip> getMyTrips(
            Authentication authentication) {

        User user = getCurrentUser(authentication);

        return tripService.getMyTrips(user);
    }

    // =====================================================
    // GET TRIP
    // Owner OR Member
    // =====================================================

    @GetMapping("/{id}")
    public Trip getTripById(
            @PathVariable Integer id,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        return tripService.getTripById(
                id,
                currentUser
        );
    }

    // =====================================================
    // UPDATE TRIP
    // Owner OR Member
    // =====================================================

    @PutMapping("/{id}")
    public Trip updateTrip(
            @PathVariable Integer id,
            @RequestBody Trip trip,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        return tripService.updateTrip(
                id,
                trip,
                currentUser
        );
    }

    // =====================================================
    // DELETE TRIP
    // Owner OR Group Admin ONLY
    // =====================================================

    @DeleteMapping("/{id}")
    public String deleteTrip(
            @PathVariable Integer id,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        tripService.deleteTrip(
                id,
                currentUser
        );

        return "Trip deleted successfully";
    }
}