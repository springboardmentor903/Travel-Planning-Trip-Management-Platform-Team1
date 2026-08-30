package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.entity.JoinRequest;
import com.tripnest.tripnest_backend.entity.JoinRequestStatus;
import com.tripnest.tripnest_backend.entity.MembershipRole;
import com.tripnest.tripnest_backend.entity.Trip;
import com.tripnest.tripnest_backend.entity.TripMembership;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.JoinRequestRepository;
import com.tripnest.tripnest_backend.repository.TripMembershipRepository;
import com.tripnest.tripnest_backend.repository.TripRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JoinRequestService {

    private final JoinRequestRepository joinRequestRepository;
    private final TripRepository tripRepository;
    private final TripMembershipRepository membershipRepository;
    private final TripAccessService tripAccessService;

    public JoinRequestService(
            JoinRequestRepository joinRequestRepository,
            TripRepository tripRepository,
            TripMembershipRepository membershipRepository,
            TripAccessService tripAccessService) {

        this.joinRequestRepository = joinRequestRepository;
        this.tripRepository = tripRepository;
        this.membershipRepository = membershipRepository;
        this.tripAccessService = tripAccessService;
    }

    // =====================================================
    // SEARCH TRIPS BY NAME
    // =====================================================

    public List<Trip> searchTrips(String tripName) {

        if (tripName == null ||
                tripName.trim().isEmpty()) {

            throw new RuntimeException(
                    "Trip name is required"
            );
        }

        return tripRepository
                .findByDestination_NameContainingIgnoreCase(
                        tripName.trim()
                );
    }

    // =====================================================
    // SEND JOIN REQUEST
    // =====================================================

    public JoinRequest createRequest(
        Long tripId,
        User currentUser) {

        Trip trip = tripRepository
                .findById(tripId.intValue())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Trip not found"
                        )
                );

        // Trip owner cannot request to join own trip
        if (trip.getUser() != null &&
                trip.getUser().getId()
                        .equals(currentUser.getId())) {

            throw new RuntimeException(
                    "Trip owner is already part of this trip"
            );
        }

        // Already a member
        if (membershipRepository
                .existsByTripIdAndUserId(
                        tripId,
                        currentUser.getId().longValue()
                )) {

            throw new RuntimeException(
                    "You are already a member of this trip"
            );
        }

        // Duplicate request
        if (joinRequestRepository
                .existsByTripIdAndUserId(
                        tripId.intValue(),
                        currentUser.getId()
                )) {

            throw new RuntimeException(
                    "Join request already exists"
            );
        }

        JoinRequest request = new JoinRequest();

        request.setTrip(trip);
        request.setUser(currentUser);
        request.setStatus(
                JoinRequestStatus.PENDING
        );

        return joinRequestRepository.save(request);
    }

    // =====================================================
    // GET JOIN REQUESTS FOR ADMIN
    // =====================================================

    public List<JoinRequest> getRequests(
        Long tripId,
        User currentUser) {

        // Only owner or Group Admin
        tripAccessService
                .checkMemberManagementAccess(
                        tripId,
                        currentUser
                );

        return joinRequestRepository
                .findByTripIdAndStatus(
                        tripId.intValue(),
                        JoinRequestStatus.PENDING
                );
    }

    // =====================================================
    // APPROVE REQUEST
    // =====================================================

    public JoinRequest approveRequest(
            Long requestId,
            User currentUser) {

        JoinRequest request =
                getRequest(requestId);

        Long tripId =
                request.getTrip().getId().longValue();

        // Only owner or Group Admin
        tripAccessService
                .checkMemberManagementAccess(
                        tripId,
                        currentUser
                );

        if (request.getStatus()
                != JoinRequestStatus.PENDING) {

            throw new RuntimeException(
                    "Join request has already been processed"
            );
        }

        User user = request.getUser();

        // Make sure user is not already a member
        if (!membershipRepository
                .existsByTripIdAndUserId(
                        tripId,
                        user.getId().longValue()
                )) {

            TripMembership membership =
                    new TripMembership();

            membership.setTrip(request.getTrip());
            membership.setUser(user);
            membership.setRole(
                    MembershipRole.MEMBER
            );

            membershipRepository.save(membership);
        }

        request.setStatus(
                JoinRequestStatus.APPROVED
        );

        return joinRequestRepository.save(request);
    }

    // =====================================================
    // REJECT REQUEST
    // =====================================================

    public JoinRequest rejectRequest(
            Long requestId,
            User currentUser) {

        JoinRequest request =
                getRequest(requestId);

        Long tripId =
                request.getTrip().getId().longValue();

        // Only owner or Group Admin
        tripAccessService
                .checkMemberManagementAccess(
                        tripId,
                        currentUser
                );

        if (request.getStatus()
                != JoinRequestStatus.PENDING) {

            throw new RuntimeException(
                    "Join request has already been processed"
            );
        }

        request.setStatus(
                JoinRequestStatus.REJECTED
        );

        return joinRequestRepository.save(request);
    }

    // =====================================================
    // GET REQUEST
    // =====================================================

    private JoinRequest getRequest(
            Long requestId) {

        return joinRequestRepository
                .findById(requestId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Join request not found"
                        )
                );
    }
}