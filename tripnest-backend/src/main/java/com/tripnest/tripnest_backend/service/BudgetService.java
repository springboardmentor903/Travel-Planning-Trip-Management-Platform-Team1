package com.tripnest.tripnest_backend.service;


import com.tripnest.tripnest_backend.entity.Budget;
import com.tripnest.tripnest_backend.repository.BudgetRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;

    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    // Create Budget
    public Budget createBudget(Budget budget) {

        if (budget.getTripId() == null) {
            throw new RuntimeException("Trip ID is required");
        }

        if (budget.getTotalBudget() == null ||
                budget.getTotalBudget().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Total budget must be greater than zero");
        }

        // Check if budget already exists
        if (budgetRepository.findByTripId(budget.getTripId()).isPresent()) {
            throw new RuntimeException(
                    "Budget already exists for this trip"
            );
        }

        return budgetRepository.save(budget);
    }


    // Update Budget
    public Budget updateBudget(Long tripId, Budget updatedBudget) {

        Budget existingBudget = budgetRepository
                .findByTripId(tripId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Budget not found for trip ID: " + tripId
                        )
                );

        if (updatedBudget.getTotalBudget() == null ||
                updatedBudget.getTotalBudget()
                        .compareTo(BigDecimal.ZERO) <= 0) {

            throw new RuntimeException(
                    "Total budget must be greater than zero"
            );
        }

        existingBudget.setTotalBudget(
                updatedBudget.getTotalBudget()
        );

        existingBudget.setAccommodationBudget(
                updatedBudget.getAccommodationBudget()
        );

        existingBudget.setFoodBudget(
                updatedBudget.getFoodBudget()
        );

        existingBudget.setTransportBudget(
                updatedBudget.getTransportBudget()
        );

        existingBudget.setActivityBudget(
                updatedBudget.getActivityBudget()
        );

        existingBudget.setMiscellaneousBudget(
                updatedBudget.getMiscellaneousBudget()
        );

        return budgetRepository.save(existingBudget);
    }


    // Get Budget by Trip ID
    public Budget getBudgetByTripId(Long tripId) {

        return budgetRepository
                .findByTripId(tripId)
                .orElse(null);
    }
}