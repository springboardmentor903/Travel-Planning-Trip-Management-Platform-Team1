package com.tripnest.tripnest_backend.controller;

import com.tripnest.tripnest_backend.entity.Budget;
import com.tripnest.tripnest_backend.service.BudgetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budgets")
@CrossOrigin
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }


    // Create Budget
    @PostMapping
    public ResponseEntity<Budget> createBudget(
            @RequestBody Budget budget) {

        Budget createdBudget =
                budgetService.createBudget(budget);

        return new ResponseEntity<>(
                createdBudget,
                HttpStatus.CREATED
        );
    }


    // Update Budget
    @PutMapping("/trip/{tripId}")
    public ResponseEntity<Budget> updateBudget(
            @PathVariable Long tripId,
            @RequestBody Budget budget) {

        Budget updatedBudget =
                budgetService.updateBudget(
                        tripId,
                        budget
                );

        return ResponseEntity.ok(updatedBudget);
    }


    // Get Budget by Trip ID
    @GetMapping("/trip/{tripId}")
    public ResponseEntity<Budget> getBudgetByTripId(
            @PathVariable Long tripId) {

        Budget budget =
                budgetService.getBudgetByTripId(tripId);

        if (budget == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(budget);
    }
}