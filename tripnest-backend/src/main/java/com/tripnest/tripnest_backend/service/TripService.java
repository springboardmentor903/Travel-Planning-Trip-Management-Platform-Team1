package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.entity.Trip;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.TripRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TripService {

    private final TripRepository tripRepository;

    public TripService(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    public Trip createTrip(Trip trip) {
        return tripRepository.save(trip);
    }

    public List<Trip> getMyTrips(User user) {
        return tripRepository.findByUser(user);
    }

    public Trip getTripById(Integer id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found"));
    }

    public Trip updateTrip(Integer id, Trip updatedTrip) {
        Trip trip = getTripById(id);

        trip.setDestination(updatedTrip.getDestination());
        trip.setStartDate(updatedTrip.getStartDate());
        trip.setEndDate(updatedTrip.getEndDate());
        trip.setTravelers(updatedTrip.getTravelers());
        trip.setBudget(updatedTrip.getBudget());
        trip.setStatus(updatedTrip.getStatus());

        return tripRepository.save(trip);
    }

    public void deleteTrip(Integer id) {
        Trip trip = getTripById(id);
        tripRepository.delete(trip);
    }
}