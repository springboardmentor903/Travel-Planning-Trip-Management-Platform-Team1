package com.tripnest.tripnest_backend.repository;

import com.tripnest.tripnest_backend.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Optional<Budget> findByTripId(Long tripId);

    List<Budget> findByTripIdIn(List<Long> tripIds);
}