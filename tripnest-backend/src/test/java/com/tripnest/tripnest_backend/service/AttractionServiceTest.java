package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.dto.AttractionRequest;
import com.tripnest.tripnest_backend.dto.AttractionResponse;
import com.tripnest.tripnest_backend.entity.Attraction;
import com.tripnest.tripnest_backend.entity.Destination;
import com.tripnest.tripnest_backend.repository.AttractionRepository;
import com.tripnest.tripnest_backend.repository.DestinationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttractionServiceTest {

    @Mock
    private AttractionRepository attractionRepository;

    @Mock
    private DestinationRepository destinationRepository;

    @InjectMocks
    private AttractionService attractionService;

    private Destination destination;
    private Attraction attraction;

    @BeforeEach
    void setUp() {
        destination = new Destination(1, "New Delhi");
        attraction = new Attraction(10L, destination, "India Gate", "Historic war memorial");
    }

    @Test
    void testGetAttractionsByDestination_Success() {
        when(destinationRepository.findById(1)).thenReturn(Optional.of(destination));
        when(attractionRepository.findByDestination(destination)).thenReturn(List.of(attraction));

        List<AttractionResponse> result = attractionService.getAttractionsByDestination(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("India Gate", result.get(0).getName());
        assertEquals(1, result.get(0).getDestinationId());
    }

    @Test
    void testGetAttractionsByDestination_DestinationNotFound_ThrowsException() {
        when(destinationRepository.findById(999)).thenReturn(Optional.empty());

        Exception ex = assertThrows(RuntimeException.class, () ->
                attractionService.getAttractionsByDestination(999));

        assertTrue(ex.getMessage().contains("Destination not found"));
    }

    @Test
    void testGetAttractionsByDestination_EmptyList_ReturnsEmpty() {
        when(destinationRepository.findById(1)).thenReturn(Optional.of(destination));
        when(attractionRepository.findByDestination(destination)).thenReturn(List.of());

        List<AttractionResponse> result = attractionService.getAttractionsByDestination(1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCreateAttraction_Success() {
        AttractionRequest request = new AttractionRequest("Qutub Minar", "Victory tower");
        when(destinationRepository.findById(1)).thenReturn(Optional.of(destination));
        when(attractionRepository.save(any(Attraction.class))).thenAnswer(invocation -> {
            Attraction a = invocation.getArgument(0);
            a.setId(20L);
            return a;
        });

        AttractionResponse response = attractionService.createAttraction(1, request);

        assertNotNull(response);
        assertEquals(20L, response.getId());
        assertEquals("Qutub Minar", response.getName());
        assertEquals("Victory tower", response.getShortDescription());
        assertEquals(1, response.getDestinationId());
    }

    @Test
    void testCreateAttraction_BlankName_ThrowsException() {
        AttractionRequest request = new AttractionRequest("", "Desc");

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                attractionService.createAttraction(1, request));

        assertTrue(ex.getMessage().contains("must not be blank"));
    }
}
