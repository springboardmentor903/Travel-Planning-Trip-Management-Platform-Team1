package com.tripnest.tripnest_backend.controller;

import com.tripnest.tripnest_backend.entity.MembershipRole;
import com.tripnest.tripnest_backend.entity.TripMembership;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.UserRepository;
import com.tripnest.tripnest_backend.service.TripMembershipService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips/{tripId}/members")
@CrossOrigin
public class TripMembershipController {

    private final TripMembershipService tripMembershipService;
    private final UserRepository userRepository;

    public TripMembershipController(
            TripMembershipService tripMembershipService,
            UserRepository userRepository) {

        this.tripMembershipService = tripMembershipService;
        this.userRepository = userRepository;
    }

    // =====================================================
    // ADD MEMBER
    // =====================================================

    @PostMapping
    public ResponseEntity<TripMembership> addMember(
            @PathVariable Long tripId,
            @RequestBody AddMemberRequest request,
            Authentication authentication) {

        User currentUser = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Current user not found"));

        TripMembership membership =
                tripMembershipService.addMember(
                        tripId,
                        request.getEmail(),
                        request.getRole(),
                        currentUser
                );

        return ResponseEntity.ok(membership);
    }

    // =====================================================
    // LIST MEMBERS
    // =====================================================

    @GetMapping
    public ResponseEntity<List<TripMembership>> getMembers(
            @PathVariable Long tripId,
            Authentication authentication) {

        User currentUser = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Current user not found"));

        return ResponseEntity.ok(
                tripMembershipService.getMembers(
                        tripId,
                        currentUser
                )
        );
    }

    // =====================================================
    // REMOVE MEMBER
    // =====================================================

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long tripId,
            @PathVariable Long userId,
            Authentication authentication) {

        User currentUser = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Current user not found"));

        tripMembershipService.removeMember(
                tripId,
                userId,
                currentUser
        );

        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // CHANGE MEMBER ROLE
    // =====================================================

    @PutMapping("/{userId}/role")
    public ResponseEntity<TripMembership> changeRole(
            @PathVariable Long tripId,
            @PathVariable Long userId,
            @RequestBody ChangeRoleRequest request,
            Authentication authentication) {

        User currentUser = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Current user not found"));

        TripMembership membership =
                tripMembershipService.changeRole(
                        tripId,
                        userId,
                        request.getRole(),
                        currentUser
                );

        return ResponseEntity.ok(membership);
    }

    // =====================================================
    // ADD MEMBER REQUEST
    // =====================================================

    public static class AddMemberRequest {

        private String email;
        private MembershipRole role;

        public AddMemberRequest() {
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public MembershipRole getRole() {
            return role;
        }

        public void setRole(MembershipRole role) {
            this.role = role;
        }
    }

    // =====================================================
    // CHANGE ROLE REQUEST
    // =====================================================

    public static class ChangeRoleRequest {

        private MembershipRole role;

        public ChangeRoleRequest() {
        }

        public MembershipRole getRole() {
            return role;
        }

        public void setRole(MembershipRole role) {
            this.role = role;
        }
    }
}