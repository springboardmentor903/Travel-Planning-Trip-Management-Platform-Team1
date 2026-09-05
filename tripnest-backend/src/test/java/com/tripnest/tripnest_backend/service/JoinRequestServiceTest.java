package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.entity.*;
import com.tripnest.tripnest_backend.repository.JoinRequestRepository;
import com.tripnest.tripnest_backend.repository.TripMembershipRepository;
import com.tripnest.tripnest_backend.repository.TripRepository;
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
class JoinRequestServiceTest {

    @Mock
    private JoinRequestRepository joinRequestRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TripMembershipRepository membershipRepository;

    @Mock
    private TripAccessService tripAccessService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private JoinRequestService joinRequestService;

    private User owner;
    private User requester;
    private Trip trip;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1);
        owner.setName("Trip Owner");
        owner.setEmail("owner@example.com");

        requester = new User();
        requester.setId(2);
        requester.setName("Requester Bob");
        requester.setEmail("bob@example.com");

        Destination dest = new Destination();
        dest.setId(1);
        dest.setName("Tokyo");

        trip = new Trip();
        trip.setId(20);
        trip.setUser(owner);
        trip.setDestination(dest);
    }

    @Test
    void testCreateRequest_NotifiesTripOwner() {
        when(tripRepository.findById(20)).thenReturn(Optional.of(trip));
        when(membershipRepository.existsByTripIdAndUserId(20L, 2L)).thenReturn(false);
        when(joinRequestRepository.existsByTripIdAndUserId(20, 2)).thenReturn(false);

        JoinRequest saved = new JoinRequest();
        saved.setTrip(trip);
        saved.setUser(requester);
        saved.setStatus(JoinRequestStatus.PENDING);

        when(joinRequestRepository.save(any(JoinRequest.class))).thenReturn(saved);

        JoinRequest result = joinRequestService.createRequest(20L, requester);

        assertNotNull(result);
        verify(notificationService, times(1)).createNotification(
                eq(owner),
                contains("Requester Bob requested to join your trip: Tokyo"),
                eq(NotificationType.JOIN_REQUEST),
                eq(trip)
        );
    }

    @Test
    void testApproveRequest_NotifiesRequester() {
        JoinRequest pendingRequest = new JoinRequest();
        pendingRequest.setTrip(trip);
        pendingRequest.setUser(requester);
        pendingRequest.setStatus(JoinRequestStatus.PENDING);

        when(joinRequestRepository.findById(100L)).thenReturn(Optional.of(pendingRequest));
        when(membershipRepository.existsByTripIdAndUserId(20L, 2L)).thenReturn(false);

        JoinRequest approved = new JoinRequest();
        approved.setTrip(trip);
        approved.setUser(requester);
        approved.setStatus(JoinRequestStatus.APPROVED);

        when(joinRequestRepository.save(any(JoinRequest.class))).thenReturn(approved);

        JoinRequest result = joinRequestService.approveRequest(100L, owner);

        assertNotNull(result);
        assertEquals(JoinRequestStatus.APPROVED, result.getStatus());
        verify(notificationService, times(1)).createNotification(
                eq(requester),
                contains("Your request to join Tokyo was approved."),
                eq(NotificationType.JOIN_REQUEST_APPROVED),
                eq(trip)
        );
    }

    @Test
    void testRejectRequest_NotifiesRequester() {
        JoinRequest pendingRequest = new JoinRequest();
        pendingRequest.setTrip(trip);
        pendingRequest.setUser(requester);
        pendingRequest.setStatus(JoinRequestStatus.PENDING);

        when(joinRequestRepository.findById(100L)).thenReturn(Optional.of(pendingRequest));

        JoinRequest rejected = new JoinRequest();
        rejected.setTrip(trip);
        rejected.setUser(requester);
        rejected.setStatus(JoinRequestStatus.REJECTED);

        when(joinRequestRepository.save(any(JoinRequest.class))).thenReturn(rejected);

        JoinRequest result = joinRequestService.rejectRequest(100L, owner);

        assertNotNull(result);
        assertEquals(JoinRequestStatus.REJECTED, result.getStatus());
        verify(notificationService, times(1)).createNotification(
                eq(requester),
                contains("Your request to join Tokyo was rejected."),
                eq(NotificationType.JOIN_REQUEST_REJECTED),
                eq(trip)
        );
    }
}
