package com.tripnest.tripnest_backend.repository;

import com.tripnest.tripnest_backend.entity.Notification;
import com.tripnest.tripnest_backend.entity.NotificationType;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    List<Notification> findByUserIdOrderByCreatedAtDesc(Integer userId);

    long countByUserAndIsReadFalse(User user);

    boolean existsByUserIdAndTripIdAndType(Integer userId, Integer tripId, NotificationType type);

    boolean existsByUserIdAndTypeAndReferenceId(Integer userId, NotificationType type, Long referenceId);

    boolean existsByTripIdAndTypeAndReferenceId(Integer tripId, NotificationType type, Long referenceId);

    boolean existsByUserIdAndTripIdAndTypeAndReferenceId(Integer userId, Integer tripId, NotificationType type, Long referenceId);

    List<Notification> findByTrip(Trip trip);
}
