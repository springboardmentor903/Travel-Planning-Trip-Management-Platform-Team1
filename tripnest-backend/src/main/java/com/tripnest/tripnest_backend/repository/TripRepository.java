package com.tripnest.tripnest_backend.repository;

import com.tripnest.tripnest_backend.entity.Trip;
import com.tripnest.tripnest_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Integer> {

    List<Trip> findByUser(User user);

    List<Trip> findByDestination_NameContainingIgnoreCase(String name);

    @Query("SELECT DISTINCT t FROM Trip t WHERE t.user = :user OR t.id IN (SELECT tm.trip.id FROM TripMembership tm WHERE tm.user = :user)")
    List<Trip> findMyTrips(@Param("user") User user);
}