package com.tripnest.tripnest_backend.controller;

import com.tripnest.tripnest_backend.entity.Activity;
import com.tripnest.tripnest_backend.service.ActivityService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/itineraries")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    // CREATE ACTIVITY
    @PostMapping("/{itineraryId}/activities")
    public Activity createActivity(
            @PathVariable Integer itineraryId,
            @RequestBody Activity activity) {

        return activityService.createActivity(itineraryId, activity);
    }

    // GET ACTIVITIES
    @GetMapping("/{itineraryId}/activities")
    public List<Activity> getActivities(
            @PathVariable Integer itineraryId) {

        return activityService.getActivities(itineraryId);
    }

    // UPDATE ACTIVITY
    @PutMapping("/{itineraryId}/activities/{activityId}")
    public Activity updateActivity(
            @PathVariable Integer itineraryId,
            @PathVariable Integer activityId,
            @RequestBody Activity activity) {

        return activityService.updateActivity(
                itineraryId,
                activityId,
                activity
        );
    }

    // DELETE ACTIVITY
    @DeleteMapping("/{itineraryId}/activities/{activityId}")
    public String deleteActivity(
            @PathVariable Integer itineraryId,
            @PathVariable Integer activityId) {

        activityService.deleteActivity(itineraryId, activityId);

        return "Activity deleted successfully";
    }
}