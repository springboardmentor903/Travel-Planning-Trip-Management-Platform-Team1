package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.entity.*;
import com.tripnest.tripnest_backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripDeleteTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TripAccessService tripAccessService;

    @Mock
    private TripMembershipRepository tripMembershipRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ItineraryRepository itineraryRepository;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private TripInvitationRepository tripInvitationRepository;

    @Mock
    private JoinRequestRepository joinRequestRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @InjectMocks
    private TripService tripService;

    private User owner;
    private Trip trip;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1);
        owner.setEmail("owner@example.com");

        trip = new Trip();
        trip.setId(100);
        trip.setUser(owner);
        trip.setStatus("PLANNED");
        trip.setStartDate(LocalDate.now().plusDays(10));
        trip.setEndDate(LocalDate.now().plusDays(15));
    }

    @Test
    void testDeleteTrip_CascadesAllChildRecords() {
        when(tripRepository.findById(100)).thenReturn(Optional.of(trip));

        Notification notif = new Notification();
        when(notificationRepository.findByTrip(trip)).thenReturn(List.of(notif));

        Itinerary itinerary = new Itinerary();
        itinerary.setId(10);
        itinerary.setTrip(trip);
        when(itineraryRepository.findByTrip(trip)).thenReturn(List.of(itinerary));

        Activity activity = new Activity();
        activity.setId(20);
        activity.setItinerary(itinerary);
        when(activityRepository.findByItinerary(itinerary)).thenReturn(List.of(activity));

        TripInvitation invitation = new TripInvitation();
        when(tripInvitationRepository.findByTripId(100)).thenReturn(List.of(invitation));

        JoinRequest joinRequest = new JoinRequest();
        when(joinRequestRepository.findByTripId(100)).thenReturn(List.of(joinRequest));

        TripMembership membership = new TripMembership();
        when(tripMembershipRepository.findByTripId(100L)).thenReturn(List.of(membership));

        Expense expense = new Expense();
        when(expenseRepository.findByTripId(100L)).thenReturn(List.of(expense));

        Budget budget = new Budget();
        budget.setTripId(100L);
        budget.setTotalBudget(new BigDecimal("5000"));
        when(budgetRepository.findByTripId(100L)).thenReturn(Optional.of(budget));

        // Execute deletion
        tripService.deleteTrip(100, owner);

        // Verify authorization check
        verify(tripAccessService).checkMemberManagementAccess(100L, owner);

        // Verify child records cleanup
        verify(notificationRepository).deleteAll(List.of(notif));
        verify(activityRepository).deleteAll(List.of(activity));
        verify(itineraryRepository).deleteAll(List.of(itinerary));
        verify(tripInvitationRepository).deleteAll(List.of(invitation));
        verify(joinRequestRepository).deleteAll(List.of(joinRequest));
        verify(tripMembershipRepository).deleteAll(List.of(membership));
        verify(expenseRepository).deleteAll(List.of(expense));
        verify(budgetRepository).delete(budget);

        // Verify trip itself is deleted
        verify(tripRepository).delete(trip);
    }
}
