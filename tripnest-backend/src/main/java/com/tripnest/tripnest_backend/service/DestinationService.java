package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.entity.Destination;
import com.tripnest.tripnest_backend.repository.DestinationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DestinationService {

    private final DestinationRepository destinationRepository;

    public DestinationService(DestinationRepository destinationRepository) {
        this.destinationRepository = destinationRepository;
    }

    public List<Destination> getAllDestinations() {
        return destinationRepository.findAll();
    }

    public Optional<Destination> getDestinationById(Integer id) {
        return destinationRepository.findById(id);
    }
}