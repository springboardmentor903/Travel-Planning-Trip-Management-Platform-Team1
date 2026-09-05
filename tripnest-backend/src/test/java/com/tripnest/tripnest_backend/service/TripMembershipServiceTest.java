package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.entity.*;
import com.tripnest.tripnest_backend.repository.TripMembershipRepository;
import com.tripnest.tripnest_backend.repository.TripRepository;
import com.tripnest.tripnest_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripMembershipServiceTest {

    @Mock
    private TripMembershipRepository membershipRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TripAccessService tripAccessService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TripMembershipService tripMembershipService;

    private User owner;
    private User newMember;
    private Trip trip;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1);
        owner.setEmail("owner@example.com");

        newMember = new User();
        newMember.setId(2);
        newMember.setEmail("member@example.com");

        Destination dest = new Destination();
        dest.setId(1);
        dest.setName("Paris");

        trip = new Trip();
        trip.setId(10);
        trip.setUser(owner);
        trip.setDestination(dest);
    }

    @Test
    void testAddMember_TriggersNotification() {
        when(tripRepository.findById(10)).thenReturn(Optional.of(trip));
        when(userRepository.findByEmail("member@example.com")).thenReturn(Optional.of(newMember));
        when(membershipRepository.existsByTripIdAndUserId(10L, 2L)).thenReturn(false);

        TripMembership savedMembership = new TripMembership();
        savedMembership.setTrip(trip);
        savedMembership.setUser(newMember);
        savedMembership.setRole(MembershipRole.MEMBER);

        when(membershipRepository.save(any(TripMembership.class))).thenReturn(savedMembership);

        TripMembership result = tripMembershipService.addMember(10L, "member@example.com", MembershipRole.MEMBER, owner);

        assertNotNull(result);
        verify(membershipRepository, times(1)).save(any(TripMembership.class));
        verify(notificationService, times(1)).createNotification(
                eq(newMember),
                contains("You have been added to the trip: Paris"),
                eq(NotificationType.MEMBER_ADDED),
                eq(trip)
        );
    }
}
