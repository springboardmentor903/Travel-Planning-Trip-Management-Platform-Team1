package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.dto.TripInvitationDTO;
import com.tripnest.tripnest_backend.entity.*;
import com.tripnest.tripnest_backend.repository.TripInvitationRepository;
import com.tripnest.tripnest_backend.repository.TripMembershipRepository;
import com.tripnest.tripnest_backend.repository.TripRepository;
import com.tripnest.tripnest_backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TripInvitationService {

    private final TripInvitationRepository invitationRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TripMembershipRepository membershipRepository;
    private final TripAccessService tripAccessService;
    private final EmailService emailService;
    private final NotificationService notificationService;

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    public TripInvitationService(
            TripInvitationRepository invitationRepository,
            TripRepository tripRepository,
            UserRepository userRepository,
            TripMembershipRepository membershipRepository,
            TripAccessService tripAccessService,
            EmailService emailService,
            NotificationService notificationService) {

        this.invitationRepository = invitationRepository;
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.tripAccessService = tripAccessService;
        this.emailService = emailService;
        this.notificationService = notificationService;
    }

    // Set base URL programmatically for testing if needed
    public void setFrontendBaseUrl(String frontendBaseUrl) {
        this.frontendBaseUrl = frontendBaseUrl;
    }

    // =====================================================
    // 1. CREATE & SEND INVITATION
    // =====================================================
    @Transactional
    public TripInvitationDTO sendInvitation(
            Long tripId,
            String email,
            MembershipRole role,
            User currentUser) {

        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email is required to invite a member");
        }

        // Validate inviter permission (Owner or GROUP_ADMIN)
        tripAccessService.checkMemberManagementAccess(tripId, currentUser);

        Trip trip = tripRepository.findById(tripId.intValue())
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        User invitee = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new RuntimeException("No registered TripNest user found with email: " + email));

        // Cannot invite trip owner
        if (trip.getUser() != null && trip.getUser().getId().equals(invitee.getId())) {
            throw new RuntimeException("Trip owner is already the host of this trip");
        }

        // Cannot invite already active member
        if (membershipRepository.existsByTripIdAndUserId(tripId, invitee.getId().longValue())) {
            throw new RuntimeException("User is already an active member of this trip");
        }

        // Check for existing pending invitation
        Optional<TripInvitation> existingPending = invitationRepository
                .findByTripIdAndInviteeIdAndStatus(tripId.intValue(), invitee.getId(), TripInvitationStatus.PENDING);

        if (existingPending.isPresent()) {
            TripInvitation pending = existingPending.get();
            if (!pending.isExpired()) {
                throw new RuntimeException("An invitation is already pending for " + email + ". Please wait for them to respond.");
            } else {
                pending.setStatus(TripInvitationStatus.EXPIRED);
                invitationRepository.save(pending);
            }
        }

        // Create secure invitation
        TripInvitation invitation = new TripInvitation();
        invitation.setTrip(trip);
        invitation.setInviter(currentUser);
        invitation.setInvitee(invitee);
        invitation.setRole(role != null ? role : MembershipRole.MEMBER);
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setStatus(TripInvitationStatus.PENDING);
        invitation.setCreatedAt(LocalDateTime.now());
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7));

        TripInvitation saved = invitationRepository.save(invitation);
        log.info("Trip invitation created: id={}, token={} for invitee={}", saved.getId(), saved.getToken(), invitee.getEmail());

        String tripName = (trip.getDestination() != null && trip.getDestination().getName() != null)
                ? trip.getDestination().getName()
                : "Trip #" + trip.getId();

        // 1. Dispatch HTML invitation email fail-safely
        try {
            emailService.sendTripInvitationEmail(currentUser, invitee, trip, saved.getToken(), frontendBaseUrl);
        } catch (Exception e) {
            log.warn("Failed to dispatch trip invitation email for invitation {}: {}", saved.getId(), e.getMessage());
        }

        // 2. Dispatch in-app notification to invitee
        try {
            notificationService.createNotification(
                    invitee,
                    currentUser.getName() + " invited you to join the trip: " + tripName,
                    NotificationType.MEMBER_ADDED,
                    trip
            );
        } catch (Exception e) {
            log.warn("Failed to create in-app notification for invitee {}: {}", invitee.getEmail(), e.getMessage());
        }

        return TripInvitationDTO.fromEntity(saved);
    }

    // =====================================================
    // 2. GET INVITATION BY TOKEN
    // =====================================================
    @Transactional
    public TripInvitationDTO getInvitationByToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new RuntimeException("Invitation token is required");
        }

        TripInvitation invitation = invitationRepository.findByToken(token.trim())
                .orElseThrow(() -> new RuntimeException("Invitation not found or invalid link"));

        if (invitation.getStatus() == TripInvitationStatus.PENDING && invitation.isExpired()) {
            invitation.setStatus(TripInvitationStatus.EXPIRED);
            invitation = invitationRepository.save(invitation);
        }

        return TripInvitationDTO.fromEntity(invitation);
    }

    // =====================================================
    // 3. ACCEPT INVITATION
    // =====================================================
    @Transactional
    public TripInvitationDTO acceptInvitation(String token, User currentUser) {
        if (token == null || token.trim().isEmpty()) {
            throw new RuntimeException("Invitation token is required");
        }
        if (currentUser == null) {
            throw new RuntimeException("Authentication required to accept invitation");
        }

        TripInvitation invitation = invitationRepository.findByToken(token.trim())
                .orElseThrow(() -> new RuntimeException("Invitation not found or invalid link"));

        // Check expiration
        if (invitation.isExpired() || invitation.getStatus() == TripInvitationStatus.EXPIRED) {
            invitation.setStatus(TripInvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            throw new RuntimeException("This invitation has expired. Please ask the trip owner to send a new invitation.");
        }

        // Check already accepted/rejected
        if (invitation.getStatus() == TripInvitationStatus.ACCEPTED) {
            throw new RuntimeException("This invitation has already been accepted.");
        }
        if (invitation.getStatus() == TripInvitationStatus.REJECTED) {
            throw new RuntimeException("This invitation was previously rejected.");
        }
        if (invitation.getStatus() != TripInvitationStatus.PENDING) {
            throw new RuntimeException("Invitation is no longer pending.");
        }

        // Security check: Verify authenticated user matches invitee
        if (invitation.getInvitee() == null || !invitation.getInvitee().getId().equals(currentUser.getId())) {
            String invitedEmail = invitation.getInvitee() != null ? invitation.getInvitee().getEmail() : "another user";
            throw new RuntimeException("Access denied: This invitation was sent to " + invitedEmail + ". You are currently logged in as " + currentUser.getEmail() + ".");
        }

        Trip trip = invitation.getTrip();
        Long tripId = trip.getId().longValue();

        // Grant trip membership if not already present
        if (!membershipRepository.existsByTripIdAndUserId(tripId, currentUser.getId().longValue())) {
            TripMembership membership = new TripMembership();
            membership.setTrip(trip);
            membership.setUser(currentUser);
            membership.setRole(invitation.getRole() != null ? invitation.getRole() : MembershipRole.MEMBER);
            membershipRepository.save(membership);
            log.info("TripMembership created for user {} on trip {}", currentUser.getEmail(), tripId);
        }

        invitation.setStatus(TripInvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(LocalDateTime.now());
        TripInvitation saved = invitationRepository.save(invitation);

        String tripName = (trip.getDestination() != null && trip.getDestination().getName() != null)
                ? trip.getDestination().getName()
                : "Trip #" + trip.getId();

        // Notify inviter/admin
        if (invitation.getInviter() != null) {
            try {
                notificationService.createNotification(
                        invitation.getInviter(),
                        currentUser.getName() + " accepted your invitation to join " + tripName + ".",
                        NotificationType.JOIN_REQUEST_APPROVED,
                        trip
                );
            } catch (Exception e) {
                log.warn("Failed to create in-app notification for inviter on accept: {}", e.getMessage());
            }

            try {
                emailService.sendInvitationAcceptedEmail(invitation.getInviter(), currentUser, trip);
            } catch (Exception e) {
                log.warn("Failed to send acceptance email to inviter: {}", e.getMessage());
            }
        }

        return TripInvitationDTO.fromEntity(saved);
    }

    // =====================================================
    // 4. REJECT INVITATION
    // =====================================================
    @Transactional
    public TripInvitationDTO rejectInvitation(String token, User currentUser) {
        if (token == null || token.trim().isEmpty()) {
            throw new RuntimeException("Invitation token is required");
        }
        if (currentUser == null) {
            throw new RuntimeException("Authentication required to reject invitation");
        }

        TripInvitation invitation = invitationRepository.findByToken(token.trim())
                .orElseThrow(() -> new RuntimeException("Invitation not found or invalid link"));

        if (invitation.isExpired() || invitation.getStatus() == TripInvitationStatus.EXPIRED) {
            invitation.setStatus(TripInvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            throw new RuntimeException("This invitation has expired.");
        }

        if (invitation.getStatus() == TripInvitationStatus.ACCEPTED) {
            throw new RuntimeException("This invitation has already been accepted.");
        }
        if (invitation.getStatus() == TripInvitationStatus.REJECTED) {
            throw new RuntimeException("This invitation was already rejected.");
        }
        if (invitation.getStatus() != TripInvitationStatus.PENDING) {
            throw new RuntimeException("Invitation is no longer pending.");
        }

        // Security check: Verify authenticated user matches invitee
        if (invitation.getInvitee() == null || !invitation.getInvitee().getId().equals(currentUser.getId())) {
            String invitedEmail = invitation.getInvitee() != null ? invitation.getInvitee().getEmail() : "another user";
            throw new RuntimeException("Access denied: This invitation was sent to " + invitedEmail + ". You are currently logged in as " + currentUser.getEmail() + ".");
        }

        invitation.setStatus(TripInvitationStatus.REJECTED);
        invitation.setRejectedAt(LocalDateTime.now());
        TripInvitation saved = invitationRepository.save(invitation);

        Trip trip = invitation.getTrip();
        String tripName = (trip != null && trip.getDestination() != null && trip.getDestination().getName() != null)
                ? trip.getDestination().getName()
                : (trip != null ? "Trip #" + trip.getId() : "the trip");

        // Notify inviter
        if (invitation.getInviter() != null && trip != null) {
            try {
                notificationService.createNotification(
                        invitation.getInviter(),
                        currentUser.getName() + " rejected your invitation to join " + tripName + ".",
                        NotificationType.JOIN_REQUEST_REJECTED,
                        trip
                );
            } catch (Exception e) {
                log.warn("Failed to create in-app notification for inviter on reject: {}", e.getMessage());
            }

            try {
                emailService.sendInvitationRejectedEmail(invitation.getInviter(), currentUser, trip);
            } catch (Exception e) {
                log.warn("Failed to send rejection email to inviter: {}", e.getMessage());
            }
        }

        return TripInvitationDTO.fromEntity(saved);
    }

    // =====================================================
    // 5. LIST INVITATIONS FOR TRIP
    // =====================================================
    @Transactional(readOnly = true)
    public List<TripInvitationDTO> getInvitationsForTrip(Long tripId, User currentUser) {
        tripAccessService.checkMemberManagementAccess(tripId, currentUser);
        return invitationRepository.findByTripId(tripId.intValue())
                .stream()
                .map(TripInvitationDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
