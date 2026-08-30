package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.entity.Trip;
import com.tripnest.tripnest_backend.entity.TripMembership;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.TripMembershipRepository;
import com.tripnest.tripnest_backend.repository.TripRepository;
import org.springframework.stereotype.Service;

@Service
public class TripAccessService {

    private final TripRepository tripRepository;
    private final TripMembershipRepository membershipRepository;

    public TripAccessService(
            TripRepository tripRepository,
            TripMembershipRepository membershipRepository) {

        this.tripRepository = tripRepository;
        this.membershipRepository = membershipRepository;
    }

    // =====================================================
    // CHECK TRIP ACCESS
    // =====================================================

    public boolean hasAccess(Long tripId, User user) {

        if (tripId == null || user == null || user.getId() == null) {
            return false;
        }

        Trip trip = tripRepository.findById(tripId.intValue())
                .orElse(null);

        if (trip == null) {
            return false;
        }

        // Trip Owner has access
        if (trip.getUser() != null &&
                trip.getUser().getId().equals(user.getId())) {

            return true;
        }

        // Trip Member has access
        return membershipRepository
                .existsByTripIdAndUserId(
                        tripId,
                        user.getId().longValue()
                );
    }


    // =====================================================
    // REQUIRE TRIP ACCESS
    // =====================================================

    public void checkAccess(Long tripId, User user) {

        if (!hasAccess(tripId, user)) {
            throw new RuntimeException(
                    "You do not have access to this trip"
            );
        }
    }


    // =====================================================
    // CHECK GROUP ADMIN / TRIP OWNER
    // =====================================================

    public boolean canManageMembers(Long tripId, User user) {

        if (tripId == null || user == null || user.getId() == null) {
            return false;
        }

        Trip trip = tripRepository.findById(tripId.intValue())
                .orElse(null);

        if (trip == null) {
            return false;
        }

        // Trip Owner can manage members
        if (trip.getUser() != null &&
                trip.getUser().getId().equals(user.getId())) {

            return true;
        }

        // GROUP_ADMIN can manage members
        TripMembership membership =
                membershipRepository
                        .findByTripIdAndUserId(
                                tripId,
                                user.getId().longValue()
                        )
                        .orElse(null);

        return membership != null &&
                membership.getRole().name().equals("GROUP_ADMIN");
    }


    // =====================================================
    // REQUIRE MEMBER MANAGEMENT ACCESS
    // =====================================================

    public void checkMemberManagementAccess(
            Long tripId,
            User user) {

        if (!canManageMembers(tripId, user)) {
            throw new RuntimeException(
                    "Only trip owner or group admin can manage members"
            );
        }
    }
}