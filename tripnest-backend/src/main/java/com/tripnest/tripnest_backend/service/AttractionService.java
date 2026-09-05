package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.dto.AttractionRequest;
import com.tripnest.tripnest_backend.dto.AttractionResponse;
import com.tripnest.tripnest_backend.entity.Attraction;
import com.tripnest.tripnest_backend.entity.Destination;
import com.tripnest.tripnest_backend.repository.AttractionRepository;
import com.tripnest.tripnest_backend.repository.DestinationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttractionService {

    private final AttractionRepository attractionRepository;
    private final DestinationRepository destinationRepository;

    @Transactional(readOnly = true)
    public List<AttractionResponse> getAttractionsByDestination(Integer destinationId) {
        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new RuntimeException("Destination not found with id: " + destinationId));

        return attractionRepository.findByDestination(destination)
                .stream()
                .map(AttractionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public AttractionResponse createAttraction(Integer destinationId, AttractionRequest request) {
        if (request == null || request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Attraction name must not be blank");
        }

        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new RuntimeException("Destination not found with id: " + destinationId));

        Attraction attraction = new Attraction();
        attraction.setDestination(destination);
        attraction.setName(request.getName().trim());
        attraction.setShortDescription(request.getShortDescription() != null ? request.getShortDescription().trim() : null);

        Attraction saved = attractionRepository.save(attraction);
        return AttractionResponse.fromEntity(saved);
    }
}
