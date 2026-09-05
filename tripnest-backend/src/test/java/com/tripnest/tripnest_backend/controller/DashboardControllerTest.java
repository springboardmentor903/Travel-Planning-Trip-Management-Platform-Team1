package com.tripnest.tripnest_backend.controller;

import com.tripnest.tripnest_backend.dto.AdminDashboardResponse;
import com.tripnest.tripnest_backend.dto.TravelerDashboardResponse;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.DestinationRepository;
import com.tripnest.tripnest_backend.repository.TripRepository;
import com.tripnest.tripnest_backend.repository.UserRepository;
import com.tripnest.tripnest_backend.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DestinationRepository destinationRepository;

    @Mock
    private DashboardService dashboardService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DashboardController dashboardController;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1);
        currentUser.setEmail("traveler@example.com");
        currentUser.setName("Traveler One");
    }

    @Test
    void testGetTravelerDashboard_Unauthenticated() {
        ResponseEntity<?> response = dashboardController.getTravelerDashboard(null);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testGetTravelerDashboard_Success() {
        when(authentication.getName()).thenReturn("traveler@example.com");
        when(userRepository.findByEmail("traveler@example.com")).thenReturn(Optional.of(currentUser));

        TravelerDashboardResponse mockResp = new TravelerDashboardResponse();
        when(dashboardService.getTravelerDashboard(currentUser)).thenReturn(mockResp);

        ResponseEntity<?> response = dashboardController.getTravelerDashboard(authentication);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResp, response.getBody());
    }

    @Test
    void testGetAdminDashboard_Success() {
        AdminDashboardResponse mockResp = new AdminDashboardResponse();
        when(dashboardService.getAdminDashboard()).thenReturn(mockResp);

        ResponseEntity<?> response = dashboardController.getAdminDashboard();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResp, response.getBody());
    }
}
