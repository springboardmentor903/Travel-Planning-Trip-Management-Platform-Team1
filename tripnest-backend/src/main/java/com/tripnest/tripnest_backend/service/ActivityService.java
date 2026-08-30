package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.entity.Activity;
import com.tripnest.tripnest_backend.entity.Itinerary;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.ActivityRepository;
import com.tripnest.tripnest_backend.repository.ItineraryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ItineraryRepository itineraryRepository;
    private final TripAccessService tripAccessService;

    public ActivityService(
            ActivityRepository activityRepository,
            ItineraryRepository itineraryRepository,
            TripAccessService tripAccessService) {

        this.activityRepository = activityRepository;
        this.itineraryRepository = itineraryRepository;
        this.tripAccessService = tripAccessService;
    }

    // CREATE
    public Activity createActivity(
            Integer itineraryId,
            Activity activity,
            User currentUser) {

        Itinerary itinerary = itineraryRepository
                .findById(itineraryId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Itinerary not found"
                        )
                );

        tripAccessService.checkAccess(
                itinerary.getTrip().getId().longValue(),
                currentUser
        );

        activity.setItinerary(itinerary);

        return activityRepository.save(activity);
    }

    // GET ALL
    public List<Activity> getActivities(
            Integer itineraryId,
            User currentUser) {

        Itinerary itinerary = itineraryRepository
                .findById(itineraryId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Itinerary not found"
                        )
                );

        tripAccessService.checkAccess(
                itinerary.getTrip().getId().longValue(),
                currentUser
        );

        return activityRepository.findByItinerary(itinerary);
    }

    // UPDATE
    public Activity updateActivity(
            Integer itineraryId,
            Integer activityId,
            Activity updatedActivity,
            User currentUser) {

        Itinerary itinerary = itineraryRepository
                .findById(itineraryId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Itinerary not found"
                        )
                );

        tripAccessService.checkAccess(
                itinerary.getTrip().getId().longValue(),
                currentUser
        );

        Activity activity = activityRepository
                .findByIdAndItinerary(
                        activityId,
                        itinerary
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Activity not found in this itinerary"
                        )
                );

        activity.setActivityName(
                updatedActivity.getActivityName()
        );

        activity.setDescription(
                updatedActivity.getDescription()
        );

        activity.setLocation(
                updatedActivity.getLocation()
        );

        activity.setStartTime(
                updatedActivity.getStartTime()
        );

        activity.setEndTime(
                updatedActivity.getEndTime()
        );

        return activityRepository.save(activity);
    }

    // DELETE
    public void deleteActivity(
            Integer itineraryId,
            Integer activityId,
            User currentUser) {

        Itinerary itinerary = itineraryRepository
                .findById(itineraryId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Itinerary not found"
                        )
                );

        tripAccessService.checkAccess(
                itinerary.getTrip().getId().longValue(),
                currentUser
        );

        Activity activity = activityRepository
                .findByIdAndItinerary(
                        activityId,
                        itinerary
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Activity not found in this itinerary"
                        )
                );

        activityRepository.delete(activity);
    }
}