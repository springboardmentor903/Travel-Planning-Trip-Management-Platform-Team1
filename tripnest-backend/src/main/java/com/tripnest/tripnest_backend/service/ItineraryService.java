package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.entity.Itinerary;
import com.tripnest.tripnest_backend.entity.Trip;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.ItineraryRepository;
import com.tripnest.tripnest_backend.repository.TripRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItineraryService {

    private final ItineraryRepository itineraryRepository;
    private final TripRepository tripRepository;
    private final TripAccessService tripAccessService;

    public ItineraryService(
            ItineraryRepository itineraryRepository,
            TripRepository tripRepository,
            TripAccessService tripAccessService) {

        this.itineraryRepository = itineraryRepository;
        this.tripRepository = tripRepository;
        this.tripAccessService = tripAccessService;
    }

    // CREATE
    public Itinerary createItinerary(
            Integer tripId,
            Itinerary itinerary,
            User currentUser) {

        tripAccessService.checkAccess(
                tripId.longValue(),
                currentUser
        );

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() ->
                        new RuntimeException("Trip not found"));

        itinerary.setTrip(trip);

        return itineraryRepository.save(itinerary);
    }

    // GET
    public List<Itinerary> getItinerariesByTrip(
            Integer tripId,
            User currentUser) {

        tripAccessService.checkAccess(
                tripId.longValue(),
                currentUser
        );

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() ->
                        new RuntimeException("Trip not found"));

        return itineraryRepository.findByTrip(trip);
    }

    // UPDATE
    public Itinerary updateItinerary(
            Integer tripId,
            Integer itineraryId,
            Itinerary updatedItinerary,
            User currentUser) {

        tripAccessService.checkAccess(
                tripId.longValue(),
                currentUser
        );

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
                    "Itinerary does not belong to this trip"
            );
        }

        itinerary.setDayNumber(
                updatedItinerary.getDayNumber()
        );

        itinerary.setDate(
                updatedItinerary.getDate()
        );

        itinerary.setTitle(
                updatedItinerary.getTitle()
        );

        itinerary.setDescription(
                updatedItinerary.getDescription()
        );

        return itineraryRepository.save(itinerary);
    }

    // DELETE
    public void deleteItinerary(
            Integer tripId,
            Integer itineraryId,
            User currentUser) {

        tripAccessService.checkAccess(
                tripId.longValue(),
                currentUser
        );

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
                    "Itinerary does not belong to this trip"
            );
        }

        itineraryRepository.delete(itinerary);
    }
}