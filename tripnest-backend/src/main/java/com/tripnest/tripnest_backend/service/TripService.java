package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.entity.*;
import com.tripnest.tripnest_backend.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class TripService {

    private final TripRepository tripRepository;
    private final TripAccessService tripAccessService;
    private final TripMembershipRepository tripMembershipRepository;
    private final NotificationService notificationService;
    private final ItineraryRepository itineraryRepository;
    private final ActivityRepository activityRepository;
    private final TripInvitationRepository tripInvitationRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final NotificationRepository notificationRepository;
    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;

    public TripService(
            TripRepository tripRepository,
            TripAccessService tripAccessService,
            TripMembershipRepository tripMembershipRepository,
            NotificationService notificationService,
            ItineraryRepository itineraryRepository,
            ActivityRepository activityRepository,
            TripInvitationRepository tripInvitationRepository,
            JoinRequestRepository joinRequestRepository,
            NotificationRepository notificationRepository,
            ExpenseRepository expenseRepository,
            BudgetRepository budgetRepository) {

        this.tripRepository = tripRepository;
        this.tripAccessService = tripAccessService;
        this.tripMembershipRepository = tripMembershipRepository;
        this.notificationService = notificationService;
        this.itineraryRepository = itineraryRepository;
        this.activityRepository = activityRepository;
        this.tripInvitationRepository = tripInvitationRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.notificationRepository = notificationRepository;
        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
    }

    // =====================================================
    // CREATE TRIP
    // =====================================================

    public Trip createTrip(Trip trip) {
        return tripRepository.save(trip);
    }

    // =====================================================
    // MY TRIPS
    // =====================================================

    public List<Trip> getMyTrips(User user) {
        return tripRepository.findMyTrips(user);
    }

    // =====================================================
    // GET TRIP
    // Owner OR Member
    // =====================================================

    public Trip getTripById(
            Integer id,
            User currentUser) {

        tripAccessService.checkAccess(
                id.longValue(),
                currentUser
        );

        return tripRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Trip not found"));
    }

    // =====================================================
    // UPDATE TRIP
    // Owner OR Member
    // =====================================================

    public Trip updateTrip(
            Integer id,
            Trip updatedTrip,
            User currentUser) {

        tripAccessService.checkAccess(
                id.longValue(),
                currentUser
        );

        Trip trip = tripRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Trip not found"));

        // Capture previous core values before updating
        Destination oldDestination = trip.getDestination();
        Integer oldDestinationId = oldDestination != null ? oldDestination.getId() : null;
        String oldDestinationName = oldDestination != null && oldDestination.getName() != null
                ? oldDestination.getName()
                : "Trip #" + trip.getId();
        LocalDate oldStartDate = trip.getStartDate();
        LocalDate oldEndDate = trip.getEndDate();

        // Apply updates
        trip.setDestination(
                updatedTrip.getDestination());

        trip.setStartDate(
                updatedTrip.getStartDate());

        trip.setEndDate(
                updatedTrip.getEndDate());

        trip.setTravelers(
                updatedTrip.getTravelers());

        trip.setBudget(
                updatedTrip.getBudget());

        trip.setStatus(
                updatedTrip.getStatus());

        Trip savedTrip = tripRepository.save(trip);

        // Check if core details changed: destination, startDate, or endDate
        Destination newDestination = savedTrip.getDestination();
        Integer newDestinationId = newDestination != null ? newDestination.getId() : null;
        String newDestinationName = newDestination != null && newDestination.getName() != null
                ? newDestination.getName()
                : "new destination";
        LocalDate newStartDate = savedTrip.getStartDate();
        LocalDate newEndDate = savedTrip.getEndDate();

        boolean destinationChanged = newDestinationId != null && !Objects.equals(oldDestinationId, newDestinationId);
        boolean datesChanged = (newStartDate != null && !Objects.equals(oldStartDate, newStartDate))
                || (newEndDate != null && !Objects.equals(oldEndDate, newEndDate));

        if (destinationChanged || datesChanged) {
            sendTravelUpdateNotifications(savedTrip, oldDestinationName, newDestinationName,
                    destinationChanged, datesChanged, currentUser);
        }

        return savedTrip;
    }

    private void sendTravelUpdateNotifications(
            Trip trip,
            String oldDestinationName,
            String newDestinationName,
            boolean destinationChanged,
            boolean datesChanged,
            User currentUser) {

        try {
            String message;
            if (destinationChanged && datesChanged) {
                message = "Trip update: " + oldDestinationName + " trip destination and travel dates have been updated.";
            } else if (destinationChanged) {
                message = "Trip update: The destination for " + oldDestinationName + " has been changed to " + newDestinationName + ".";
            } else {
                message = "Trip update: The dates for " + oldDestinationName + " have been updated.";
            }

            Set<User> otherMembers = new HashSet<>();

            // Notify trip owner if owner is not the one who made the change
            if (trip.getUser() != null && (currentUser == null || !trip.getUser().getId().equals(currentUser.getId()))) {
                otherMembers.add(trip.getUser());
            }

            // Notify other trip members
            if (trip.getId() != null) {
                List<TripMembership> memberships = tripMembershipRepository.findByTripId(trip.getId().longValue());
                for (TripMembership tm : memberships) {
                    if (tm.getUser() != null && (currentUser == null || !tm.getUser().getId().equals(currentUser.getId()))) {
                        otherMembers.add(tm.getUser());
                    }
                }
            }

            for (User member : otherMembers) {
                try {
                    notificationService.createNotification(member, message, NotificationType.TRAVEL_UPDATE, trip);
                    log.info("Travel update notification sent to {} for trip {}", member.getEmail(), trip.getId());
                } catch (Exception ex) {
                    log.warn("Failed to send travel update notification to {}: {}", member.getEmail(), ex.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error dispatching travel update notifications for trip {}: {}", trip.getId(), e.getMessage(), e);
        }
    }

    // =====================================================
    // DELETE TRIP
    // Owner OR Group Admin ONLY
    // =====================================================

    @Transactional
    public void deleteTrip(
            Integer id,
            User currentUser) {

        tripAccessService.checkMemberManagementAccess(
                id.longValue(),
                currentUser
        );

        Trip trip = tripRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Trip not found"));

        Long tripIdLong = id.longValue();

        // 1. Delete all notifications referencing this trip
        List<Notification> notifications = notificationRepository.findByTrip(trip);
        if (notifications != null && !notifications.isEmpty()) {
            notificationRepository.deleteAll(notifications);
        }

        // 2. Delete all activities and itineraries for this trip
        List<Itinerary> itineraries = itineraryRepository.findByTrip(trip);
        if (itineraries != null && !itineraries.isEmpty()) {
            for (Itinerary itinerary : itineraries) {
                List<Activity> activities = activityRepository.findByItinerary(itinerary);
                if (activities != null && !activities.isEmpty()) {
                    activityRepository.deleteAll(activities);
                }
            }
            itineraryRepository.deleteAll(itineraries);
        }

        // 3. Delete all invitations for this trip
        List<TripInvitation> invitations = tripInvitationRepository.findByTripId(id);
        if (invitations != null && !invitations.isEmpty()) {
            tripInvitationRepository.deleteAll(invitations);
        }

        // 4. Delete all join requests for this trip
        List<JoinRequest> joinRequests = joinRequestRepository.findByTripId(id);
        if (joinRequests != null && !joinRequests.isEmpty()) {
            joinRequestRepository.deleteAll(joinRequests);
        }

        // 5. Delete all memberships for this trip
        List<TripMembership> memberships = tripMembershipRepository.findByTripId(tripIdLong);
        if (memberships != null && !memberships.isEmpty()) {
            tripMembershipRepository.deleteAll(memberships);
        }

        // 6. Delete all expenses for this trip
        List<Expense> expenses = expenseRepository.findByTripId(tripIdLong);
        if (expenses != null && !expenses.isEmpty()) {
            expenseRepository.deleteAll(expenses);
        }

        // 7. Delete budget for this trip
        budgetRepository.findByTripId(tripIdLong).ifPresent(budgetRepository::delete);

        // 8. Delete the trip itself
        tripRepository.delete(trip);
        log.info("Successfully deleted trip ID: {} and all associated data", id);
    }
}