package com.tripnest.tripnest_backend.repository;

import com.tripnest.tripnest_backend.entity.Activity;
import com.tripnest.tripnest_backend.entity.Itinerary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActivityRepository extends JpaRepository<Activity, Integer> {

    List<Activity> findByItinerary(Itinerary itinerary);

    Optional<Activity> findByIdAndItinerary(Integer id, Itinerary itinerary);
}