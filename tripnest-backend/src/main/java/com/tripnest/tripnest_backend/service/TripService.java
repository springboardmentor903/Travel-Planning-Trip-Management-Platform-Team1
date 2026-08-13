package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.entity.Trip;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.TripRepository;
import com.tripnest.tripnest_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    public TripService(
            TripRepository tripRepository,
            UserRepository userRepository) {

        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
    }

    public Trip createTrip(Trip trip, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        trip.setUser(user);

        return tripRepository.save(trip);
    }

    public List<Trip> getMyTripsByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return tripRepository.findByUser(user);
    }

    public Optional<Trip> getTripById(Integer id) {
        return tripRepository.findById(id);
    }

    public Trip updateTrip(Trip trip) {
        return tripRepository.save(trip);
    }

    public void deleteTrip(Integer id) {
        tripRepository.deleteById(id);
    }
}