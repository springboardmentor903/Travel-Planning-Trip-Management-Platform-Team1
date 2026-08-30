package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.entity.Trip;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.TripRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TripService {

    private final TripRepository tripRepository;
    private final TripAccessService tripAccessService;

    public TripService(
            TripRepository tripRepository,
            TripAccessService tripAccessService) {

        this.tripRepository = tripRepository;
        this.tripAccessService = tripAccessService;
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

        return tripRepository.save(trip);
    }

    // =====================================================
    // DELETE TRIP
    // Owner OR Group Admin ONLY
    // =====================================================

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

        tripRepository.delete(trip);
    }
}