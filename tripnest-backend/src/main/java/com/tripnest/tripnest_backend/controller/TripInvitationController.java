package com.tripnest.tripnest_backend.controller;

import com.tripnest.tripnest_backend.dto.TripInvitationDTO;
import com.tripnest.tripnest_backend.entity.MembershipRole;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.UserRepository;
import com.tripnest.tripnest_backend.service.TripInvitationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequiredArgsConstructor
public class TripInvitationController {

    private final TripInvitationService tripInvitationService;
    private final UserRepository userRepository;

    // =====================================================
    // 1. SEND INVITATION (Trip Owner/Admin)
    // =====================================================
    @PostMapping("/api/trips/{tripId}/invitations")
    public ResponseEntity<?> sendInvitation(
            @PathVariable Long tripId,
            @RequestBody SendInvitationRequest request,
            Authentication authentication) {

        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication required"));
        }

        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        try {
            TripInvitationDTO dto = tripInvitationService.sendInvitation(
                    tripId,
                    request.getEmail(),
                    request.getRole(),
                    currentUser
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    // =====================================================
    // 2. LIST INVITATIONS FOR TRIP (Trip Owner/Admin)
    // =====================================================
    @GetMapping("/api/trips/{tripId}/invitations")
    public ResponseEntity<?> getTripInvitations(
            @PathVariable Long tripId,
            Authentication authentication) {

        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication required"));
        }

        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        try {
            List<TripInvitationDTO> list = tripInvitationService.getInvitationsForTrip(tripId, currentUser);
            return ResponseEntity.ok(list);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    // =====================================================
    // 3. GET INVITATION DETAILS (Public Preview)
    // =====================================================
    @GetMapping("/api/trip-invitations/public/{token}")
    public ResponseEntity<?> getInvitationDetails(@PathVariable String token) {
        try {
            TripInvitationDTO dto = tripInvitationService.getInvitationByToken(token);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    // =====================================================
    // 4. ACCEPT INVITATION (Invited User)
    // =====================================================
    @PostMapping("/api/trip-invitations/{token}/accept")
    public ResponseEntity<?> acceptInvitation(
            @PathVariable String token,
            Authentication authentication) {

        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication required to accept invitation"));
        }

        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        try {
            TripInvitationDTO dto = tripInvitationService.acceptInvitation(token, currentUser);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", ex.getMessage()));
            }
            if (ex.getMessage() != null && ex.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", ex.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    // =====================================================
    // 5. REJECT INVITATION (Invited User)
    // =====================================================
    @PostMapping("/api/trip-invitations/{token}/reject")
    public ResponseEntity<?> rejectInvitation(
            @PathVariable String token,
            Authentication authentication) {

        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication required to reject invitation"));
        }

        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        try {
            TripInvitationDTO dto = tripInvitationService.rejectInvitation(token, currentUser);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", ex.getMessage()));
            }
            if (ex.getMessage() != null && ex.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", ex.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    @Data
    public static class SendInvitationRequest {
        private String email;
        private MembershipRole role;
    }
}
