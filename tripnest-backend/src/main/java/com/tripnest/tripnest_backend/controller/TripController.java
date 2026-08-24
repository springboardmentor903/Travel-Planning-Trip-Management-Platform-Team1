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

    public TripController(TripService tripService, UserRepository userRepository) {
        this.tripService = tripService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public Trip createTrip(@RequestBody Trip trip, Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        trip.setUser(user);

        return tripService.createTrip(trip);
    }

    // OLD CODE - kept for reference
    // @GetMapping
    // public List<Trip> getMyTrips(Authentication authentication) {

    // FIX - supports both /api/trips and /api/trips/my for authenticated user trips
    @GetMapping({"", "/my"})
    public List<Trip> getMyTrips(Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return tripService.getMyTrips(user);
    }

    @GetMapping("/{id}")
    public Trip getTripById(@PathVariable Integer id) {
        return tripService.getTripById(id);
    }

    @PutMapping("/{id}")
    public Trip updateTrip(
            @PathVariable Integer id,
            @RequestBody Trip trip) {

        return tripService.updateTrip(id, trip);
    }

    @DeleteMapping("/{id}")
    public String deleteTrip(@PathVariable Integer id) {

        tripService.deleteTrip(id);

        return "Trip deleted successfully";
    }
}