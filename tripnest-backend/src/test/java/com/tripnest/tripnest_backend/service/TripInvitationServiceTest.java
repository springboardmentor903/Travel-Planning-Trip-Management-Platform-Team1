package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.dto.TripInvitationDTO;
import com.tripnest.tripnest_backend.entity.*;
import com.tripnest.tripnest_backend.repository.TripInvitationRepository;
import com.tripnest.tripnest_backend.repository.TripMembershipRepository;
import com.tripnest.tripnest_backend.repository.TripRepository;
import com.tripnest.tripnest_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripInvitationServiceTest {

    @Mock
    private TripInvitationRepository invitationRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TripMembershipRepository membershipRepository;

    @Mock
    private TripAccessService tripAccessService;

    @Mock
    private EmailService emailService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TripInvitationService invitationService;

    private User owner;
    private User invitee;
    private User foreignUser;
    private Trip trip;

    @BeforeEach
    void setUp() {
        invitationService.setFrontendBaseUrl("http://localhost:5173");

        owner = new User();
        owner.setId(1);
        owner.setName("Trip Owner");
        owner.setEmail("owner@example.com");

        invitee = new User();
        invitee.setId(2);
        invitee.setName("Alice Invitee");
        invitee.setEmail("alice@example.com");

        foreignUser = new User();
        foreignUser.setId(3);
        foreignUser.setName("Bob Foreign");
        foreignUser.setEmail("bob@example.com");

        Destination destination = new Destination();
        destination.setId(10);
        destination.setName("Paris");

        trip = new Trip();
        trip.setId(100);
        trip.setUser(owner);
        trip.setDestination(destination);
    }

    @Test
    void testSendInvitation_Success() {
        when(tripRepository.findById(100)).thenReturn(Optional.of(trip));
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(invitee));
        when(membershipRepository.existsByTripIdAndUserId(100L, 2L)).thenReturn(false);
        when(invitationRepository.findByTripIdAndInviteeIdAndStatus(100, 2, TripInvitationStatus.PENDING))
                .thenReturn(Optional.empty());

        when(invitationRepository.save(any(TripInvitation.class))).thenAnswer(invocation -> {
            TripInvitation inv = invocation.getArgument(0);
            inv.setId(50L);
            return inv;
        });

        TripInvitationDTO dto = invitationService.sendInvitation(100L, "alice@example.com", MembershipRole.MEMBER, owner);

        assertNotNull(dto);
        assertEquals(50L, dto.getId());
        assertEquals("alice@example.com", dto.getInviteeEmail());
        assertEquals(TripInvitationStatus.PENDING, dto.getStatus());

        verify(tripAccessService, times(1)).checkMemberManagementAccess(100L, owner);
        verify(emailService, times(1)).sendTripInvitationEmail(eq(owner), eq(invitee), eq(trip), anyString(), eq("http://localhost:5173"));
        verify(notificationService, times(1)).createNotification(eq(invitee), anyString(), eq(NotificationType.MEMBER_ADDED), eq(trip));
    }

    @Test
    void testSendInvitation_CannotInviteSelfOwner() {
        when(tripRepository.findById(100)).thenReturn(Optional.of(trip));
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                invitationService.sendInvitation(100L, "owner@example.com", MembershipRole.MEMBER, owner)
        );

        assertTrue(ex.getMessage().contains("Trip owner is already the host"));
        verify(invitationRepository, never()).save(any());
    }

    @Test
    void testSendInvitation_CannotInviteExistingMember() {
        when(tripRepository.findById(100)).thenReturn(Optional.of(trip));
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(invitee));
        when(membershipRepository.existsByTripIdAndUserId(100L, 2L)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                invitationService.sendInvitation(100L, "alice@example.com", MembershipRole.MEMBER, owner)
        );

        assertTrue(ex.getMessage().contains("already an active member"));
        verify(invitationRepository, never()).save(any());
    }

    @Test
    void testSendInvitation_DuplicatePendingThrows() {
        TripInvitation existing = new TripInvitation();
        existing.setId(10L);
        existing.setStatus(TripInvitationStatus.PENDING);
        existing.setExpiresAt(LocalDateTime.now().plusDays(5));

        when(tripRepository.findById(100)).thenReturn(Optional.of(trip));
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(invitee));
        when(membershipRepository.existsByTripIdAndUserId(100L, 2L)).thenReturn(false);
        when(invitationRepository.findByTripIdAndInviteeIdAndStatus(100, 2, TripInvitationStatus.PENDING))
                .thenReturn(Optional.of(existing));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                invitationService.sendInvitation(100L, "alice@example.com", MembershipRole.MEMBER, owner)
        );

        assertTrue(ex.getMessage().contains("already pending"));
        verify(emailService, never()).sendTripInvitationEmail(any(), any(), any(), any(), any());
    }

    @Test
    void testAcceptInvitation_Success_CreatesMembershipAndNotifies() {
        TripInvitation invitation = new TripInvitation();
        invitation.setId(1L);
        invitation.setToken("valid-token-123");
        invitation.setTrip(trip);
        invitation.setInviter(owner);
        invitation.setInvitee(invitee);
        invitation.setStatus(TripInvitationStatus.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(3));

        when(invitationRepository.findByToken("valid-token-123")).thenReturn(Optional.of(invitation));
        when(membershipRepository.existsByTripIdAndUserId(100L, 2L)).thenReturn(false);
        when(invitationRepository.save(any(TripInvitation.class))).thenAnswer(invocation_args -> invocation_args.getArgument(0));

        TripInvitationDTO result = invitationService.acceptInvitation("valid-token-123", invitee);

        assertNotNull(result);
        assertEquals(TripInvitationStatus.ACCEPTED, result.getStatus());

        verify(membershipRepository, times(1)).save(any(TripMembership.class));
        verify(notificationService, times(1)).createNotification(eq(owner), anyString(), eq(NotificationType.JOIN_REQUEST_APPROVED), eq(trip));
        verify(emailService, times(1)).sendInvitationAcceptedEmail(eq(owner), eq(invitee), eq(trip));
    }

    @Test
    void testAcceptInvitation_ExpiredThrows() {
        TripInvitation invitation = new TripInvitation();
        invitation.setId(1L);
        invitation.setToken("expired-token");
        invitation.setTrip(trip);
        invitation.setInviter(owner);
        invitation.setInvitee(invitee);
        invitation.setStatus(TripInvitationStatus.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().minusDays(1)); // Expired

        when(invitationRepository.findByToken("expired-token")).thenReturn(Optional.of(invitation));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                invitationService.acceptInvitation("expired-token", invitee)
        );

        assertTrue(ex.getMessage().contains("expired"));
        verify(membershipRepository, never()).save(any());
    }

    @Test
    void testAcceptInvitation_WrongUserThrowsAccessDenied() {
        TripInvitation invitation = new TripInvitation();
        invitation.setId(1L);
        invitation.setToken("token-for-alice");
        invitation.setTrip(trip);
        invitation.setInviter(owner);
        invitation.setInvitee(invitee); // Belongs to Alice
        invitation.setStatus(TripInvitationStatus.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(3));

        when(invitationRepository.findByToken("token-for-alice")).thenReturn(Optional.of(invitation));

        // Foreign user Bob attempts to accept Alice's invitation
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                invitationService.acceptInvitation("token-for-alice", foreignUser)
        );

        assertTrue(ex.getMessage().contains("Access denied"));
        verify(membershipRepository, never()).save(any());
    }

    @Test
    void testRejectInvitation_Success() {
        TripInvitation invitation = new TripInvitation();
        invitation.setId(1L);
        invitation.setToken("token-to-reject");
        invitation.setTrip(trip);
        invitation.setInviter(owner);
        invitation.setInvitee(invitee);
        invitation.setStatus(TripInvitationStatus.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(3));

        when(invitationRepository.findByToken("token-to-reject")).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any(TripInvitation.class))).thenAnswer(invocation_args -> invocation_args.getArgument(0));

        TripInvitationDTO result = invitationService.rejectInvitation("token-to-reject", invitee);

        assertNotNull(result);
        assertEquals(TripInvitationStatus.REJECTED, result.getStatus());

        verify(membershipRepository, never()).save(any());
        verify(notificationService, times(1)).createNotification(eq(owner), anyString(), eq(NotificationType.JOIN_REQUEST_REJECTED), eq(trip));
        verify(emailService, times(1)).sendInvitationRejectedEmail(eq(owner), eq(invitee), eq(trip));
    }
}
