package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.entity.*;
import com.tripnest.tripnest_backend.repository.ActivityRepository;
import com.tripnest.tripnest_backend.repository.NotificationRepository;
import com.tripnest.tripnest_backend.repository.TripMembershipRepository;
import com.tripnest.tripnest_backend.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReminderSchedulerServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private TripMembershipRepository membershipRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ReminderSchedulerService reminderSchedulerService;

    private User owner;
    private User member;
    private Trip upcomingTrip;
    private Trip farTrip;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1);
        owner.setEmail("owner@example.com");

        member = new User();
        member.setId(2);
        member.setEmail("member@example.com");

        Destination dest = new Destination(1, "Paris");

        upcomingTrip = new Trip();
        upcomingTrip.setId(10);
        upcomingTrip.setUser(owner);
        upcomingTrip.setDestination(dest);
        upcomingTrip.setStartDate(LocalDate.now().plusDays(1)); // Tomorrow
        upcomingTrip.setEndDate(LocalDate.now().plusDays(5));
        upcomingTrip.setStatus("UPCOMING");

        farTrip = new Trip();
        farTrip.setId(20);
        farTrip.setUser(owner);
        farTrip.setDestination(dest);
        farTrip.setStartDate(LocalDate.now().plusDays(10)); // 10 days away
        farTrip.setEndDate(LocalDate.now().plusDays(15));
        farTrip.setStatus("UPCOMING");
    }

    @Test
    void testSendUpcomingTripReminders_NotifiesOwnerAndMembers() {
        when(tripRepository.findAll()).thenReturn(List.of(upcomingTrip, farTrip));
        TripMembership tm = new TripMembership();
        tm.setUser(member);
        tm.setTrip(upcomingTrip);
        when(membershipRepository.findByTripId(10L)).thenReturn(List.of(tm));
        when(notificationRepository.existsByUserIdAndTripIdAndType(anyInt(), anyInt(), eq(NotificationType.TRIP_REMINDER)))
                .thenReturn(false);

        reminderSchedulerService.sendUpcomingTripReminders();

        verify(notificationService, times(1)).createNotification(
                eq(owner), contains("Paris"), eq(NotificationType.TRIP_REMINDER), eq(upcomingTrip));
        verify(notificationService, times(1)).createNotification(
                eq(member), contains("Paris"), eq(NotificationType.TRIP_REMINDER), eq(upcomingTrip));
    }

    @Test
    void testSendUpcomingTripReminders_PreventsDuplicateReminders() {
        when(tripRepository.findAll()).thenReturn(List.of(upcomingTrip));
        when(membershipRepository.findByTripId(10L)).thenReturn(List.of());
        // Already notified owner
        when(notificationRepository.existsByUserIdAndTripIdAndType(1, 10, NotificationType.TRIP_REMINDER))
                .thenReturn(true);

        reminderSchedulerService.sendUpcomingTripReminders();

        verify(notificationService, never()).createNotification(any(), anyString(), any(), any());
    }

    @Test
    void testSendUpcomingActivityReminders_NotifiesWithin24Hours() {
        LocalDateTime targetTime = LocalDateTime.now().plusHours(2);

        Itinerary itinerary = new Itinerary();
        itinerary.setId(100);
        itinerary.setTrip(upcomingTrip);
        itinerary.setDate(targetTime.toLocalDate());

        Activity activity = new Activity();
        activity.setId(50);
        activity.setActivityName("Eiffel Tower Tour");
        activity.setItinerary(itinerary);
        activity.setStartTime(targetTime.toLocalTime());

        when(activityRepository.findAll()).thenReturn(List.of(activity));
        TripMembership tm = new TripMembership();
        tm.setUser(member);
        tm.setTrip(upcomingTrip);
        when(membershipRepository.findByTripId(10L)).thenReturn(List.of(tm));
        when(notificationRepository.existsByUserIdAndTypeAndReferenceId(anyInt(), eq(NotificationType.ACTIVITY_REMINDER), eq(50L)))
                .thenReturn(false);

        reminderSchedulerService.sendUpcomingActivityReminders();

        verify(notificationService, times(1)).createNotification(
                eq(owner), contains("Eiffel Tower Tour"), eq(NotificationType.ACTIVITY_REMINDER), eq(upcomingTrip), eq(50L));
        verify(notificationService, times(1)).createNotification(
                eq(member), contains("Eiffel Tower Tour"), eq(NotificationType.ACTIVITY_REMINDER), eq(upcomingTrip), eq(50L));
    }

    @Test
    void testSendUpcomingActivityReminders_PreventsDuplicateReminders() {
        LocalDateTime targetTime = LocalDateTime.now().plusHours(2);

        Itinerary itinerary = new Itinerary();
        itinerary.setId(100);
        itinerary.setTrip(upcomingTrip);
        itinerary.setDate(targetTime.toLocalDate());

        Activity activity = new Activity();
        activity.setId(50);
        activity.setActivityName("Eiffel Tower Tour");
        activity.setItinerary(itinerary);
        activity.setStartTime(targetTime.toLocalTime());

        when(activityRepository.findAll()).thenReturn(List.of(activity));
        when(membershipRepository.findByTripId(10L)).thenReturn(List.of());
        // Persistent check returns true: already notified
        when(notificationRepository.existsByUserIdAndTypeAndReferenceId(1, NotificationType.ACTIVITY_REMINDER, 50L))
                .thenReturn(true);

        reminderSchedulerService.sendUpcomingActivityReminders();

        verify(notificationService, never()).createNotification(any(), anyString(), any(), any(), anyLong());
    }
}
