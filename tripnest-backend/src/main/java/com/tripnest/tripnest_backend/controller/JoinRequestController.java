package com.tripnest.tripnest_backend.controller;

import com.tripnest.tripnest_backend.entity.JoinRequest;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.service.JoinRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.tripnest.tripnest_backend.repository.UserRepository;

import java.util.List;

@RestController
@CrossOrigin
public class JoinRequestController {

    private final JoinRequestService joinRequestService;
    private final UserRepository userRepository;

    public JoinRequestController(
            JoinRequestService joinRequestService,
            UserRepository userRepository) {

        this.joinRequestService = joinRequestService;
        this.userRepository = userRepository;
    }





    // =====================================================
// SEARCH TRIPS
// =====================================================

@GetMapping("/api/trips/search")
public ResponseEntity<List<com.tripnest.tripnest_backend.entity.Trip>> searchTrips(
        @RequestParam String name) {

    return ResponseEntity.ok(
            joinRequestService.searchTrips(name)
    );
}


    // =====================================================
    // SEND JOIN REQUEST
    // =====================================================

    @PostMapping("/api/trips/{tripId}/join-requests")
    public ResponseEntity<JoinRequest> createRequest(
            @PathVariable Long tripId,
            Authentication authentication) {

        User currentUser =
        userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Current user not found"));

        JoinRequest request =
                joinRequestService.createRequest(
                        tripId,
                        currentUser
                );

        return ResponseEntity.ok(request);
    }


    // =====================================================
    // LIST JOIN REQUESTS
    // =====================================================

    @GetMapping("/api/trips/{tripId}/join-requests")
    public ResponseEntity<List<JoinRequest>> getRequests(
            @PathVariable Long tripId,
            Authentication authentication) {

        User currentUser =
        userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Current user not found"));

        return ResponseEntity.ok(
                joinRequestService.getRequests(
                        tripId,
                        currentUser
                )
        );
    }


    // =====================================================
    // APPROVE JOIN REQUEST
    // =====================================================

    @PutMapping("/api/join-requests/{requestId}/approve")
    public ResponseEntity<JoinRequest> approveRequest(
            @PathVariable Long requestId,
            Authentication authentication) {

        User currentUser =
        userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Current user not found"));

        return ResponseEntity.ok(
                joinRequestService.approveRequest(
                        requestId,
                        currentUser
                )
        );
    }


    // =====================================================
    // REJECT JOIN REQUEST
    // =====================================================

    @PutMapping("/api/join-requests/{requestId}/reject")
    public ResponseEntity<JoinRequest> rejectRequest(
            @PathVariable Long requestId,
            Authentication authentication) {

       User currentUser =
        userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Current user not found"));

        return ResponseEntity.ok(
                joinRequestService.rejectRequest(
                        requestId,
                        currentUser
                )
        );
    }
}