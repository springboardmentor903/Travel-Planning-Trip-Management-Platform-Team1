package com.tripnest.tripnest_backend.controller;

import com.tripnest.tripnest_backend.entity.Trip;
//import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.service.TripService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.List;


@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping
public ResponseEntity<Trip> createTrip(
        @RequestBody Trip trip,
        Authentication authentication) {

    String email = authentication.getName();

    return ResponseEntity.ok(
            tripService.createTrip(trip, email)
    );
}
    
   @GetMapping
public ResponseEntity<List<Trip>> getMyTrips(Authentication authentication) {

    String email = authentication.getName();

    return ResponseEntity.ok(tripService.getMyTripsByEmail(email));
}

    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTripById(@PathVariable Integer id) {
        return tripService.getTripById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Trip> updateTrip(
            @PathVariable Integer id,
            @RequestBody Trip trip) {

        trip.setId(id);
        return ResponseEntity.ok(tripService.updateTrip(trip));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(@PathVariable Integer id) {
        tripService.deleteTrip(id);
        return ResponseEntity.noContent().build();
    }
}