package com.tripnest.tripnest_backend.controller;

import com.tripnest.tripnest_backend.dto.AdminDashboardResponse;
import com.tripnest.tripnest_backend.dto.TravelerDashboardResponse;
import com.tripnest.tripnest_backend.entity.Trip;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.DestinationRepository;
import com.tripnest.tripnest_backend.repository.TripRepository;
import com.tripnest.tripnest_backend.repository.UserRepository;
import com.tripnest.tripnest_backend.service.DashboardService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final DestinationRepository destinationRepository;
    private final DashboardService dashboardService;

    public DashboardController(
            TripRepository tripRepository,
            UserRepository userRepository,
            DestinationRepository destinationRepository,
            DashboardService dashboardService) {

        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.destinationRepository = destinationRepository;
        this.dashboardService = dashboardService;
    }

    // =====================================================
    // TRAVELER DASHBOARD (AGGREGATED ENDPOINT)
    // =====================================================
    @GetMapping("/traveler")
    public ResponseEntity<?> getTravelerDashboard(Authentication authentication) {

        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication required"));
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TravelerDashboardResponse response = dashboardService.getTravelerDashboard(user);
        return ResponseEntity.ok(response);
    }

    // =====================================================
    // ADMIN DASHBOARD (AGGREGATED ENDPOINT - ADMIN ONLY)
    // =====================================================
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> getAdminDashboard() {
        AdminDashboardResponse response = dashboardService.getAdminDashboard();
        return ResponseEntity.ok(response);
    }

    // =====================================================
    // LEGACY DASHBOARD STATS (PRESERVED)
    // =====================================================
    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats(
            Authentication authentication) {

        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication required"));
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        List<Trip> trips = tripRepository.findMyTrips(user);

        LocalDate today = LocalDate.now();

        long totalTrips = trips.size();

        long upcomingTrips = trips.stream()
                .filter(trip ->
                        trip.getStartDate() != null &&
                        !trip.getStartDate().isBefore(today))
                .count();

        long pastTrips = trips.stream()
                .filter(trip ->
                        trip.getEndDate() != null &&
                        trip.getEndDate().isBefore(today))
                .count();

        long popularDestinations = destinationRepository.count();

        Map<String, Object> stats = new HashMap<>();

        stats.put("totalTrips", totalTrips);
        stats.put("upcomingTrips", upcomingTrips);
        stats.put("pastTrips", pastTrips);
        stats.put("popularDestinations", popularDestinations);

        return ResponseEntity.ok(stats);
    }
}