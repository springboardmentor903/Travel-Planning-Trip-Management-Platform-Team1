package com.tripnest.tripnest_backend.repository;

import com.tripnest.tripnest_backend.entity.JoinRequest;
import com.tripnest.tripnest_backend.entity.JoinRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JoinRequestRepository
        extends JpaRepository<JoinRequest, Long> {

    // Check if a request already exists
    boolean existsByTripIdAndUserId(
            Integer tripId,
            Integer userId
    );

    // Find a particular user's request for a trip
    Optional<JoinRequest> findByTripIdAndUserId(
            Integer tripId,
            Integer userId
    );

    // Get all requests for a trip
    List<JoinRequest> findByTripId(
            Integer tripId
    );

    // Get requests by status
    List<JoinRequest> findByTripIdAndStatus(
            Integer tripId,
            JoinRequestStatus status
    );
}