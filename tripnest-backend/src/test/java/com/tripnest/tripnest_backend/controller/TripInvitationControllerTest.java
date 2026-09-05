package com.tripnest.tripnest_backend.controller;

import com.tripnest.tripnest_backend.dto.TripInvitationDTO;
import com.tripnest.tripnest_backend.entity.MembershipRole;
import com.tripnest.tripnest_backend.entity.TripInvitationStatus;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.UserRepository;
import com.tripnest.tripnest_backend.service.TripInvitationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripInvitationControllerTest {

    @Mock
    private TripInvitationService tripInvitationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TripInvitationController tripInvitationController;

    private User user;
    private TripInvitationDTO sampleDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setName("Alice");
        user.setEmail("alice@example.com");

        sampleDto = TripInvitationDTO.builder()
                .id(10L)
                .tripId(100)
                .tripName("Paris Trip")
                .destination("Paris")
                .inviterName("Bob")
                .inviterEmail("bob@example.com")
                .inviteeName("Alice")
                .inviteeEmail("alice@example.com")
                .role(MembershipRole.MEMBER)
                .token("valid-uuid-token")
                .status(TripInvitationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .isExpired(false)
                .build();
    }

    @Test
    void testSendInvitation_Success() {
        when(authentication.getName()).thenReturn("alice@example.com");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        TripInvitationController.SendInvitationRequest req = new TripInvitationController.SendInvitationRequest();
        req.setEmail("bob@example.com");
        req.setRole(MembershipRole.MEMBER);

        when(tripInvitationService.sendInvitation(eq(100L), eq("bob@example.com"), eq(MembershipRole.MEMBER), eq(user)))
                .thenReturn(sampleDto);

        ResponseEntity<?> response = tripInvitationController.sendInvitation(100L, req, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(sampleDto, response.getBody());
    }

    @Test
    void testGetTripInvitations_Success() {
        when(authentication.getName()).thenReturn("alice@example.com");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        when(tripInvitationService.getInvitationsForTrip(eq(100L), eq(user)))
                .thenReturn(List.of(sampleDto));

        ResponseEntity<?> response = tripInvitationController.getTripInvitations(100L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(sampleDto), response.getBody());
    }

    @Test
    void testGetInvitationDetails_PublicEndpoint() {
        when(tripInvitationService.getInvitationByToken("valid-uuid-token")).thenReturn(sampleDto);

        ResponseEntity<?> response = tripInvitationController.getInvitationDetails("valid-uuid-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sampleDto, response.getBody());
    }

    @Test
    void testAcceptInvitation_Success() {
        sampleDto.setStatus(TripInvitationStatus.ACCEPTED);
        when(authentication.getName()).thenReturn("alice@example.com");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(tripInvitationService.acceptInvitation(eq("valid-uuid-token"), eq(user))).thenReturn(sampleDto);

        ResponseEntity<?> response = tripInvitationController.acceptInvitation("valid-uuid-token", authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sampleDto, response.getBody());
    }

    @Test
    void testRejectInvitation_Success() {
        sampleDto.setStatus(TripInvitationStatus.REJECTED);
        when(authentication.getName()).thenReturn("alice@example.com");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(tripInvitationService.rejectInvitation(eq("valid-uuid-token"), eq(user))).thenReturn(sampleDto);

        ResponseEntity<?> response = tripInvitationController.rejectInvitation("valid-uuid-token", authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sampleDto, response.getBody());
    }
}
