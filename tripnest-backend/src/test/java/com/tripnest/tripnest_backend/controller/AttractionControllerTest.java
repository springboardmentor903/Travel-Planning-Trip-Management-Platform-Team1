package com.tripnest.tripnest_backend.controller;

import com.tripnest.tripnest_backend.dto.AttractionRequest;
import com.tripnest.tripnest_backend.dto.AttractionResponse;
import com.tripnest.tripnest_backend.service.AttractionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttractionControllerTest {

    @Mock
    private AttractionService attractionService;

    @InjectMocks
    private AttractionController attractionController;

    @Test
    void testGetAttractions_Success() {
        AttractionResponse response = new AttractionResponse(1L, "Eiffel Tower", "Iconic monument", 10);
        when(attractionService.getAttractionsByDestination(10)).thenReturn(List.of(response));

        ResponseEntity<?> result = attractionController.getAttractionsByDestination(10);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        @SuppressWarnings("unchecked")
        List<AttractionResponse> body = (List<AttractionResponse>) result.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals("Eiffel Tower", body.get(0).getName());
    }

    @Test
    void testGetAttractions_DestinationNotFound_Returns404() {
        when(attractionService.getAttractionsByDestination(999))
                .thenThrow(new RuntimeException("Destination not found with id: 999"));

        ResponseEntity<?> result = attractionController.getAttractionsByDestination(999);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void testGetAttractions_EmptyList_Returns200() {
        when(attractionService.getAttractionsByDestination(10)).thenReturn(List.of());

        ResponseEntity<?> result = attractionController.getAttractionsByDestination(10);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        @SuppressWarnings("unchecked")
        List<AttractionResponse> body = (List<AttractionResponse>) result.getBody();
        assertNotNull(body);
        assertTrue(body.isEmpty());
    }

    @Test
    void testCreateAttraction_Success_Returns201() {
        AttractionRequest request = new AttractionRequest("Louvre Museum", "Famous art museum");
        AttractionResponse response = new AttractionResponse(2L, "Louvre Museum", "Famous art museum", 10);
        when(attractionService.createAttraction(10, request)).thenReturn(response);

        ResponseEntity<?> result = attractionController.createAttraction(10, request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        AttractionResponse body = (AttractionResponse) result.getBody();
        assertNotNull(body);
        assertEquals("Louvre Museum", body.getName());
    }

    @Test
    void testCreateAttraction_BlankName_Returns400() {
        AttractionRequest request = new AttractionRequest("   ", "Description");

        ResponseEntity<?> result = attractionController.createAttraction(10, request);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void testCreateAttraction_DestinationNotFound_Returns404() {
        AttractionRequest request = new AttractionRequest("Louvre Museum", "Famous art museum");
        when(attractionService.createAttraction(999, request))
                .thenThrow(new RuntimeException("Destination not found with id: 999"));

        ResponseEntity<?> result = attractionController.createAttraction(999, request);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }
}
