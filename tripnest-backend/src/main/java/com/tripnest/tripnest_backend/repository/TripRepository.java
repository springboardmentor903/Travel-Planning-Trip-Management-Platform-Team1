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

    @Query("SELECT COUNT(t) FROM Trip t WHERE UPPER(t.status) IN ('ACTIVE', 'ONGOING', 'IN_PROGRESS')")
    long countActiveTrips();

    @Query("SELECT COUNT(t) FROM Trip t WHERE UPPER(t.status) = 'COMPLETED'")
    long countCompletedTrips();

    @Query("SELECT t.destination.name, COUNT(t) FROM Trip t WHERE t.destination IS NOT NULL GROUP BY t.destination.name ORDER BY COUNT(t) DESC")
    List<Object[]> findPopularDestinationsAcrossPlatform();

    @Query("SELECT t.destination.name, COUNT(t) FROM Trip t WHERE (t.user = :user OR t.id IN (SELECT tm.trip.id FROM TripMembership tm WHERE tm.user = :user)) AND t.destination IS NOT NULL GROUP BY t.destination.name ORDER BY COUNT(t) DESC")
    List<Object[]> findMostVisitedDestinationsByUser(@Param("user") User user);
}