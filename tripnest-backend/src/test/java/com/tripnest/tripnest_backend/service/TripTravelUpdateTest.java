package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.entity.*;
import com.tripnest.tripnest_backend.repository.TripMembershipRepository;
import com.tripnest.tripnest_backend.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripTravelUpdateTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TripAccessService tripAccessService;

    @Mock
    private TripMembershipRepository tripMembershipRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TripService tripService;

    private User owner;
    private User member;
    private Destination paris;
    private Destination london;
    private Trip trip;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1);
        owner.setEmail("owner@example.com");

        member = new User();
        member.setId(2);
        member.setEmail("member@example.com");

        paris = new Destination(10, "Paris");
        london = new Destination(20, "London");

        trip = new Trip();
        trip.setId(100);
        trip.setUser(owner);
        trip.setDestination(paris);
        trip.setStartDate(LocalDate.of(2026, 6, 1));
        trip.setEndDate(LocalDate.of(2026, 6, 10));
        trip.setStatus("PLANNED");
        trip.setTravelers(2);
        trip.setBudget(5000.0);
    }

    @Test
    void testUpdateTrip_DestinationChange_NotifiesOtherMembersOnly() {
        Trip updatedTrip = new Trip();
        updatedTrip.setDestination(london);
        updatedTrip.setStartDate(trip.getStartDate());
        updatedTrip.setEndDate(trip.getEndDate());
        updatedTrip.setStatus(trip.getStatus());
        updatedTrip.setTravelers(trip.getTravelers());
        updatedTrip.setBudget(trip.getBudget());

        when(tripRepository.findById(100)).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TripMembership tm = new TripMembership();
        tm.setUser(member);
        when(tripMembershipRepository.findByTripId(100L)).thenReturn(List.of(tm));

        // Owner makes the change
        tripService.updateTrip(100, updatedTrip, owner);

        // Member is notified
        verify(notificationService, times(1)).createNotification(
                eq(member), contains("London"), eq(NotificationType.TRAVEL_UPDATE), any(Trip.class));
        // Owner (updater) is NOT notified
        verify(notificationService, never()).createNotification(
                eq(owner), anyString(), any(), any());
    }

    @Test
    void testUpdateTrip_DatesChange_NotifiesOtherMembers() {
        Trip updatedTrip = new Trip();
        updatedTrip.setDestination(paris); // same destination
        updatedTrip.setStartDate(LocalDate.of(2026, 7, 1)); // new date
        updatedTrip.setEndDate(LocalDate.of(2026, 7, 15));
        updatedTrip.setStatus(trip.getStatus());
        updatedTrip.setTravelers(trip.getTravelers());
        updatedTrip.setBudget(trip.getBudget());

        when(tripRepository.findById(100)).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TripMembership tm = new TripMembership();
        tm.setUser(member);
        when(tripMembershipRepository.findByTripId(100L)).thenReturn(List.of(tm));

        // Owner makes the date change
        tripService.updateTrip(100, updatedTrip, owner);

        // Member is notified about dates
        verify(notificationService, times(1)).createNotification(
                eq(member), contains("dates"), eq(NotificationType.TRAVEL_UPDATE), any(Trip.class));
    }

    @Test
    void testUpdateTrip_DestinationAndDatesChange_ProducesConsolidatedNotification() {
        Trip updatedTrip = new Trip();
        updatedTrip.setDestination(london);
        updatedTrip.setStartDate(LocalDate.of(2026, 7, 1));
        updatedTrip.setEndDate(LocalDate.of(2026, 7, 15));
        updatedTrip.setStatus(trip.getStatus());
        updatedTrip.setTravelers(trip.getTravelers());
        updatedTrip.setBudget(trip.getBudget());

        when(tripRepository.findById(100)).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TripMembership tm = new TripMembership();
        tm.setUser(member);
        when(tripMembershipRepository.findByTripId(100L)).thenReturn(List.of(tm));

        tripService.updateTrip(100, updatedTrip, owner);

        // Single consolidated notification
        verify(notificationService, times(1)).createNotification(
                eq(member), contains("destination and travel dates have been updated"), eq(NotificationType.TRAVEL_UPDATE), any(Trip.class));
    }

    @Test
    void testUpdateTrip_UnrelatedChange_DoesNotTriggerTravelUpdate() {
        Trip updatedTrip = new Trip();
        updatedTrip.setDestination(paris); // same
        updatedTrip.setStartDate(trip.getStartDate()); // same
        updatedTrip.setEndDate(trip.getEndDate()); // same
        updatedTrip.setStatus("COMPLETED"); // changed status
        updatedTrip.setTravelers(4); // changed travelers
        updatedTrip.setBudget(6000.0); // changed budget

        when(tripRepository.findById(100)).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        tripService.updateTrip(100, updatedTrip, owner);

        // No travel update notification dispatched
        verify(notificationService, never()).createNotification(any(), anyString(), any(), any());
    }
}
