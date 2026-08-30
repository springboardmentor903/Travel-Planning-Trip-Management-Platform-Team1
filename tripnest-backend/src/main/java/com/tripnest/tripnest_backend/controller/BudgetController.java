package com.tripnest.tripnest_backend.controller;

import com.tripnest.tripnest_backend.entity.Budget;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.service.BudgetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.tripnest.tripnest_backend.repository.UserRepository;

@RestController
@RequestMapping("/api/budgets")
@CrossOrigin
public class BudgetController {

    private final BudgetService budgetService;
    private final UserRepository userRepository;

    public BudgetController(
            BudgetService budgetService,
            UserRepository userRepository) {

        this.budgetService = budgetService;
        this.userRepository = userRepository;
    }

    // CREATE BUDGET
    @PostMapping
    public ResponseEntity<Budget> createBudget(
            @RequestBody Budget budget,
            Authentication authentication) {

        User currentUser =
        userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Current user not found"));

        Budget createdBudget =
                budgetService.createBudget(
                        budget,
                        currentUser
                );

        return new ResponseEntity<>(
                createdBudget,
                HttpStatus.CREATED
        );
    }

    // UPDATE BUDGET
    @PutMapping("/trip/{tripId}")
    public ResponseEntity<Budget> updateBudget(
            @PathVariable Long tripId,
            @RequestBody Budget budget,
            Authentication authentication) {

       User currentUser =
        userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Current user not found"));

        Budget updatedBudget =
                budgetService.updateBudget(
                        tripId,
                        budget,
                        currentUser
                );

        return ResponseEntity.ok(updatedBudget);
    }

    // GET BUDGET
    @GetMapping("/trip/{tripId}")
    public ResponseEntity<Budget> getBudgetByTripId(
            @PathVariable Long tripId,
            Authentication authentication) {

       User currentUser =
        userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Current user not found"));

        Budget budget =
                budgetService.getBudgetByTripId(
                        tripId,
                        currentUser
                );

        if (budget == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(budget);
    }
}