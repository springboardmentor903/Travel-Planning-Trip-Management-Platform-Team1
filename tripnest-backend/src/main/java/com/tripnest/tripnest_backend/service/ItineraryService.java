package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.entity.Itinerary;
import com.tripnest.tripnest_backend.entity.Trip;
import com.tripnest.tripnest_backend.repository.ItineraryRepository;
import com.tripnest.tripnest_backend.repository.TripRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItineraryService {

    private final ItineraryRepository itineraryRepository;
    private final TripRepository tripRepository;

    public ItineraryService(
            ItineraryRepository itineraryRepository,
            TripRepository tripRepository) {

        this.itineraryRepository = itineraryRepository;
        this.tripRepository = tripRepository;
    }

    public Itinerary createItinerary(
            Integer tripId,
            Itinerary itinerary) {

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() ->
                        new RuntimeException("Trip not found"));

        itinerary.setTrip(trip);

        return itineraryRepository.save(itinerary);
    }

    public List<Itinerary> getItinerariesByTrip(
            Integer tripId) {

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() ->
                        new RuntimeException("Trip not found"));

        return itineraryRepository.findByTrip(trip);
    }

    // UPDATE ITINERARY
    public Itinerary updateItinerary(
            Integer tripId,
            Integer itineraryId,
            Itinerary updatedItinerary) {

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() ->
                        new RuntimeException("Trip not found"));

        Itinerary itinerary = itineraryRepository
                .findById(itineraryId)
                .orElseThrow(() ->
                        new RuntimeException("Itinerary not found"));

        // Make sure itinerary belongs to this trip
        if (!itinerary.getTrip().getId().equals(trip.getId())) {
            throw new RuntimeException(
                    "Itinerary does not belong to this trip");
        }

        itinerary.setDayNumber(
                updatedItinerary.getDayNumber());

        itinerary.setDate(
                updatedItinerary.getDate());

        itinerary.setTitle(
                updatedItinerary.getTitle());

        itinerary.setDescription(
                updatedItinerary.getDescription());

        return itineraryRepository.save(itinerary);
    }

    // DELETE ITINERARY
    public void deleteItinerary(
            Integer tripId,
            Integer itineraryId) {

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() ->
                        new RuntimeException("Trip not found"));

        Itinerary itinerary = itineraryRepository
                .findById(itineraryId)
                .orElseThrow(() ->
                        new RuntimeException("Itinerary not found"));

        // Make sure itinerary belongs to this trip
        if (!itinerary.getTrip().getId().equals(trip.getId())) {
            throw new RuntimeException(
                    "Itinerary does not belong to this trip");
        }

        itineraryRepository.delete(itinerary);
    }
}