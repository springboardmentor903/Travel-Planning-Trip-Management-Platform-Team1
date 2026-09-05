package com.tripnest.tripnest_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "travel_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravelPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // One preference record belongs to one user
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Example: Adventure, Relaxation, Family, Solo, etc.
    @Column(name = "preferred_travel_type")
    private String preferredTravelType;

    // Preferred destination
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_destination_id")
    private Destination preferredDestination;

    // Favourite destination
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "favourite_destination_id")
    private Destination favouriteDestination;
}