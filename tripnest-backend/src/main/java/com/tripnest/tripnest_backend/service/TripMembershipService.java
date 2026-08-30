package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.entity.MembershipRole;
import com.tripnest.tripnest_backend.entity.Trip;
import com.tripnest.tripnest_backend.entity.TripMembership;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.TripMembershipRepository;
import com.tripnest.tripnest_backend.repository.TripRepository;
import com.tripnest.tripnest_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TripMembershipService {

    private final TripMembershipRepository membershipRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TripAccessService tripAccessService;

    public TripMembershipService(
            TripMembershipRepository membershipRepository,
            TripRepository tripRepository,
            UserRepository userRepository,
            TripAccessService tripAccessService) {

        this.membershipRepository = membershipRepository;
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.tripAccessService = tripAccessService;
    }


    // =====================================================
    // ADD MEMBER BY EMAIL
    // =====================================================

    public TripMembership addMember(
            Long tripId,
            String email,
            MembershipRole role,
            User currentUser) {

        tripAccessService.checkMemberManagementAccess(
                tripId,
                currentUser
        );

        Trip trip = getTrip(tripId);

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with email: " + email
                        )
                );

        // Trip owner cannot be added as a member
        if (trip.getUser() != null &&
                trip.getUser().getId().equals(user.getId())) {

            throw new RuntimeException(
                    "Trip owner is already part of this trip"
            );
        }

        // Check duplicate membership
        if (membershipRepository.existsByTripIdAndUserId(
                tripId,
                user.getId().longValue())) {

            throw new RuntimeException(
                    "User is already a member of this trip"
            );
        }

        TripMembership membership = new TripMembership();

        membership.setTrip(trip);
        membership.setUser(user);
        membership.setRole(role != null ? role : MembershipRole.MEMBER);

        return membershipRepository.save(membership);
    }

    public TripMembership addMember(
            Long tripId,
            String email,
            User currentUser) {

        return addMember(tripId, email, MembershipRole.MEMBER, currentUser);
    }


    // =====================================================
    // LIST MEMBERS
    // =====================================================

    public List<TripMembership> getMembers(
            Long tripId,
            User currentUser) {

        tripAccessService.checkAccess(
                tripId,
                currentUser
        );

        // Make sure trip exists
        getTrip(tripId);

        return membershipRepository.findByTripId(tripId);
    }


    // =====================================================
    // REMOVE MEMBER
    // =====================================================

    public void removeMember(
            Long tripId,
            Long userId,
            User currentUser) {

        tripAccessService.checkMemberManagementAccess(
                tripId,
                currentUser
        );

        Trip trip = getTrip(tripId);

        // Do not allow removing trip owner
        if (trip.getUser() != null &&
                trip.getUser().getId().longValue() == userId) {

            throw new RuntimeException(
                    "Trip owner cannot be removed"
            );
        }

        TripMembership membership =
                membershipRepository
                        .findByTripIdAndUserId(
                                tripId,
                                userId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Member not found"
                                )
                        );

        membershipRepository.delete(membership);
    }


    // =====================================================
    // CHANGE MEMBER ROLE
    // =====================================================

    public TripMembership changeRole(
            Long tripId,
            Long userId,
            MembershipRole newRole,
            User currentUser) {

        tripAccessService.checkMemberManagementAccess(
                tripId,
                currentUser
        );

        Trip trip = getTrip(tripId);

        // Trip owner does not have a membership role
        if (trip.getUser() != null &&
                trip.getUser().getId().longValue() == userId) {

            throw new RuntimeException(
                    "Trip owner's role cannot be changed"
            );
        }

        if (newRole == null) {
            throw new RuntimeException(
                    "Role is required"
            );
        }

        TripMembership membership =
                membershipRepository
                        .findByTripIdAndUserId(
                                tripId,
                                userId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Member not found"
                                )
                        );

        membership.setRole(newRole);

        return membershipRepository.save(membership);
    }


    // =====================================================
    // GET TRIP
    // =====================================================

    private Trip getTrip(Long tripId) {

        return tripRepository
                .findById(tripId.intValue())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Trip not found"
                        )
                );
    }
}