package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.entity.*;
import com.tripnest.tripnest_backend.repository.ActivityRepository;
import com.tripnest.tripnest_backend.repository.NotificationRepository;
import com.tripnest.tripnest_backend.repository.TripMembershipRepository;
import com.tripnest.tripnest_backend.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderSchedulerService {

    private final TripRepository tripRepository;
    private final ActivityRepository activityRepository;
    private final TripMembershipRepository membershipRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    // =====================================================
    // 1. UPCOMING TRIP REMINDERS (Runs daily at 9:00 AM)
    // =====================================================
    @Scheduled(cron = "${tripnest.scheduler.trip-reminder-cron:0 0 9 * * ?}")
    @Transactional
    public void sendUpcomingTripReminders() {
        log.info("Starting daily upcoming trip reminders check...");
        LocalDate today = LocalDate.now();
        LocalDate upcomingCutoff = today.plusDays(1); // Next 24 hours / starting tomorrow

        try {
            List<Trip> allTrips = tripRepository.findAll();
            for (Trip trip : allTrips) {
                if (trip.getStartDate() == null || "CANCELLED".equalsIgnoreCase(trip.getStatus())) {
                    continue;
                }

                // Check if trip starts today or tomorrow (upcoming window)
                if (!trip.getStartDate().isBefore(today) && !trip.getStartDate().isAfter(upcomingCutoff)) {
                    notifyTripMembersForUpcomingTrip(trip);
                }
            }
        } catch (Exception e) {
            log.error("Error during trip reminder scheduler execution: {}", e.getMessage(), e);
        }
    }

    private void notifyTripMembersForUpcomingTrip(Trip trip) {
        String destName = (trip.getDestination() != null && trip.getDestination().getName() != null)
                ? trip.getDestination().getName()
                : "Trip #" + trip.getId();

        String message = "Reminder: Your trip to " + destName + " starts on " + trip.getStartDate() + "!";

        Set<User> recipients = getTripRecipients(trip);

        for (User user : recipients) {
            try {
                // Persistent duplicate prevention
                boolean alreadyNotified = notificationRepository
                        .existsByUserIdAndTripIdAndType(user.getId(), trip.getId(), NotificationType.TRIP_REMINDER);

                if (!alreadyNotified) {
                    notificationService.createNotification(user, message, NotificationType.TRIP_REMINDER, trip);
                    log.info("Trip reminder sent to user {} for trip {}", user.getEmail(), trip.getId());
                }
            } catch (Exception ex) {
                log.warn("Failed to send trip reminder to user {}: {}", user.getEmail(), ex.getMessage());
            }
        }
    }

    // =====================================================
    // 2. UPCOMING ACTIVITY REMINDERS (Runs daily at 9:00 AM)
    // =====================================================
    @Scheduled(cron = "${tripnest.scheduler.activity-reminder-cron:0 0 9 * * ?}")
    @Transactional
    public void sendUpcomingActivityReminders() {
        log.info("Starting daily upcoming activity reminders check...");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next24Hours = now.plusHours(24);

        try {
            List<Activity> allActivities = activityRepository.findAll();
            for (Activity activity : allActivities) {
                if (activity.getItinerary() == null || activity.getItinerary().getTrip() == null) {
                    continue;
                }

                Trip trip = activity.getItinerary().getTrip();
                if ("CANCELLED".equalsIgnoreCase(trip.getStatus())) {
                    continue;
                }

                LocalDate activityDate = determineActivityDate(activity);
                if (activityDate == null) {
                    continue;
                }

                LocalTime startTime = activity.getStartTime() != null ? activity.getStartTime() : LocalTime.MIN;
                LocalDateTime activityStart = LocalDateTime.of(activityDate, startTime);

                // Activity starts within next 24 hours
                if (!activityStart.isBefore(now) && !activityStart.isAfter(next24Hours)) {
                    notifyTripMembersForActivity(activity, trip, activityDate);
                }
            }
        } catch (Exception e) {
            log.error("Error during activity reminder scheduler execution: {}", e.getMessage(), e);
        }
    }

    private void notifyTripMembersForActivity(Activity activity, Trip trip, LocalDate activityDate) {
        String destName = (trip.getDestination() != null && trip.getDestination().getName() != null)
                ? trip.getDestination().getName()
                : "Trip #" + trip.getId();

        String timeInfo = activity.getStartTime() != null ? " at " + activity.getStartTime() : "";
        String message = "Reminder: Activity '" + activity.getActivityName() + "' for your trip to "
                + destName + " is scheduled for " + activityDate + timeInfo + "!";

        Set<User> recipients = getTripRecipients(trip);

        for (User user : recipients) {
            try {
                // Persistent duplicate prevention using referenceId = activity.getId()
                boolean alreadyNotified = notificationRepository.existsByUserIdAndTypeAndReferenceId(
                        user.getId(),
                        NotificationType.ACTIVITY_REMINDER,
                        activity.getId().longValue()
                );

                if (!alreadyNotified) {
                    notificationService.createNotification(
                            user,
                            message,
                            NotificationType.ACTIVITY_REMINDER,
                            trip,
                            activity.getId().longValue()
                    );
                    log.info("Activity reminder sent to user {} for activity {}", user.getEmail(), activity.getId());
                }
            } catch (Exception ex) {
                log.warn("Failed to send activity reminder to user {}: {}", user.getEmail(), ex.getMessage());
            }
        }
    }

    private LocalDate determineActivityDate(Activity activity) {
        Itinerary itinerary = activity.getItinerary();
        if (itinerary.getDate() != null) {
            return itinerary.getDate();
        }

        Trip trip = itinerary.getTrip();
        if (trip != null && trip.getStartDate() != null && itinerary.getDayNumber() != null) {
            return trip.getStartDate().plusDays(Math.max(0, itinerary.getDayNumber() - 1));
        }

        return null;
    }

    private Set<User> getTripRecipients(Trip trip) {
        Set<User> recipients = new HashSet<>();

        // 1. Trip Owner
        if (trip.getUser() != null) {
            recipients.add(trip.getUser());
        }

        // 2. Trip Members
        if (trip.getId() != null) {
            List<TripMembership> memberships = membershipRepository.findByTripId(trip.getId().longValue());
            for (TripMembership tm : memberships) {
                if (tm.getUser() != null) {
                    recipients.add(tm.getUser());
                }
            }
        }

        return recipients;
    }
}
