package com.tripnest.tripnest_backend.dto;

import com.tripnest.tripnest_backend.entity.MembershipRole;
import com.tripnest.tripnest_backend.entity.TripInvitation;
import com.tripnest.tripnest_backend.entity.TripInvitationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripInvitationDTO {

    private Long id;
    private Integer tripId;
    private String tripName;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private String inviterName;
    private String inviterEmail;
    private String inviteeName;
    private String inviteeEmail;
    private MembershipRole role;
    private String token;
    private TripInvitationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean isExpired;

    public static TripInvitationDTO fromEntity(TripInvitation invitation) {
        if (invitation == null) {
            return null;
        }

        Integer tripId = null;
        String tripName = null;
        String destination = null;
        LocalDate startDate = null;
        LocalDate endDate = null;

        if (invitation.getTrip() != null) {
            tripId = invitation.getTrip().getId();
            startDate = invitation.getTrip().getStartDate();
            endDate = invitation.getTrip().getEndDate();
            if (invitation.getTrip().getDestination() != null) {
                destination = invitation.getTrip().getDestination().getName();
                tripName = destination;
            } else {
                tripName = "Trip #" + tripId;
            }
        }

        String inviterName = invitation.getInviter() != null ? invitation.getInviter().getName() : "Trip Host";
        String inviterEmail = invitation.getInviter() != null ? invitation.getInviter().getEmail() : "";
        String inviteeName = invitation.getInvitee() != null ? invitation.getInvitee().getName() : "Traveler";
        String inviteeEmail = invitation.getInvitee() != null ? invitation.getInvitee().getEmail() : "";

        boolean expired = invitation.isExpired() || invitation.getStatus() == TripInvitationStatus.EXPIRED;

        return TripInvitationDTO.builder()
                .id(invitation.getId())
                .tripId(tripId)
                .tripName(tripName)
                .destination(destination)
                .startDate(startDate)
                .endDate(endDate)
                .inviterName(inviterName)
                .inviterEmail(inviterEmail)
                .inviteeName(inviteeName)
                .inviteeEmail(inviteeEmail)
                .role(invitation.getRole())
                .token(invitation.getToken())
                .status(expired && invitation.getStatus() == TripInvitationStatus.PENDING ? TripInvitationStatus.EXPIRED : invitation.getStatus())
                .createdAt(invitation.getCreatedAt())
                .expiresAt(invitation.getExpiresAt())
                .isExpired(expired)
                .build();
    }
}
