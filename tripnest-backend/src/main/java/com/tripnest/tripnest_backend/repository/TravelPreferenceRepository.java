package com.tripnest.tripnest_backend.repository;

import com.tripnest.tripnest_backend.entity.TravelPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TravelPreferenceRepository
        extends JpaRepository<TravelPreference, Integer> {

    Optional<TravelPreference> findByUserId(Integer userId);
}