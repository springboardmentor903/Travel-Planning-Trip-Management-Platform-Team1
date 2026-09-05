package com.tripnest.tripnest_backend.controller;

import com.tripnest.tripnest_backend.entity.Activity;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.service.ActivityService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.tripnest.tripnest_backend.repository.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/itineraries")
public class ActivityController {

    private final ActivityService activityService;
    private final UserRepository userRepository;

    public ActivityController(
            ActivityService activityService,
            UserRepository userRepository) {

        this.activityService = activityService;
        this.userRepository = userRepository;
    }

    // CREATE ACTIVITY
    @PostMapping("/{itineraryId}/activities")
    public Activity createActivity(
            @PathVariable Integer itineraryId,
            @RequestBody Activity activity,
            Authentication authentication) {

        User currentUser =
        userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Current user not found"));

        return activityService.createActivity(
                itineraryId,
                activity,
                currentUser
        );
    }

    // GET ACTIVITIES
    @GetMapping("/{itineraryId}/activities")
    public List<Activity> getActivities(
            @PathVariable Integer itineraryId,
            Authentication authentication) {

        User currentUser =
        userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Current user not found"));

        return activityService.getActivities(
                itineraryId,
                currentUser
        );
    }

    // UPDATE ACTIVITY
    @PutMapping("/{itineraryId}/activities/{activityId}")
    public Activity updateActivity(
            @PathVariable Integer itineraryId,
            @PathVariable Integer activityId,
            @RequestBody Activity activity,
            Authentication authentication) {

        User currentUser =
        userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Current user not found"));

        return activityService.updateActivity(
                itineraryId,
                activityId,
                activity,
                currentUser
        );
    }

    // DELETE ACTIVITY
    @DeleteMapping("/{itineraryId}/activities/{activityId}")
    public String deleteActivity(
            @PathVariable Integer itineraryId,
            @PathVariable Integer activityId,
            Authentication authentication) {

        User currentUser =
        userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Current user not found"));

        activityService.deleteActivity(
                itineraryId,
                activityId,
                currentUser
        );

        return "Activity deleted successfully";
    }
}