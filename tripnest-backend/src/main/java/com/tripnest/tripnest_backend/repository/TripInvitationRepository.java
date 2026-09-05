package com.tripnest.tripnest_backend.repository;

import com.tripnest.tripnest_backend.entity.TripInvitation;
import com.tripnest.tripnest_backend.entity.TripInvitationStatus;
import com.tripnest.tripnest_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripInvitationRepository extends JpaRepository<TripInvitation, Long> {

    Optional<TripInvitation> findByToken(String token);

    Optional<TripInvitation> findByTripIdAndInviteeIdAndStatus(Integer tripId, Integer inviteeId, TripInvitationStatus status);

    boolean existsByTripIdAndInviteeIdAndStatus(Integer tripId, Integer inviteeId, TripInvitationStatus status);

    List<TripInvitation> findByTripId(Integer tripId);

    List<TripInvitation> findByTripIdAndStatus(Integer tripId, TripInvitationStatus status);

    List<TripInvitation> findByInviteeAndStatus(User invitee, TripInvitationStatus status);
}
