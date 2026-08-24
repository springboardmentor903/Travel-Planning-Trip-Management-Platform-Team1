package com.tripnest.tripnest_backend.repository;

import com.tripnest.tripnest_backend.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository
        extends JpaRepository<Expense, Long> {

    // Get all expenses of a trip
    List<Expense> findByTripId(Long tripId);


    // Find particular expense of particular trip
    Optional<Expense> findByIdAndTripId(
            Long id,
            Long tripId
    );


    // Category-wise expense summary
    @Query("""
        SELECT e.category, SUM(e.amount)
        FROM Expense e
        WHERE e.tripId = :tripId
        GROUP BY e.category
    """)
    List<Object[]> getCategorySummary(
            @Param("tripId") Long tripId
    );


    // Total expenses of a trip
    @Query("""
        SELECT COALESCE(SUM(e.amount), 0)
        FROM Expense e
        WHERE e.tripId = :tripId
    """)
    BigDecimal getTotalExpenses(
            @Param("tripId") Long tripId
    );
}