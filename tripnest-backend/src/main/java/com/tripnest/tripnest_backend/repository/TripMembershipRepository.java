package com.tripnest.tripnest_backend.repository;

import com.tripnest.tripnest_backend.entity.TripMembership;
import com.tripnest.tripnest_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TripMembershipRepository
        extends JpaRepository<TripMembership, Long> {

    // Check whether a user is a member of a trip
    Optional<TripMembership> findByTripIdAndUserId(
            Long tripId,
            Long userId
    );

    // Get all members of a trip
    List<TripMembership> findByTripId(Long tripId);

    // Check membership
    boolean existsByTripIdAndUserId(
            Long tripId,
            Long userId
    );

    // Find membership using User entity
    Optional<TripMembership> findByTripIdAndUser(
            Long tripId,
            User user
    );

    // Delete a member from a trip
    void deleteByTripIdAndUserId(
            Long tripId,
            Long userId
    );
}