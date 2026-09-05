package com.tripnest.tripnest_backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long tripId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalBudget;

    @Column(precision = 15, scale = 2)
    private BigDecimal accommodationBudget;

    @Column(precision = 15, scale = 2)
    private BigDecimal foodBudget;

    @Column(precision = 15, scale = 2)
    private BigDecimal transportBudget;

    @Column(precision = 15, scale = 2)
    private BigDecimal activityBudget;

    @Column(precision = 15, scale = 2)
    private BigDecimal miscellaneousBudget;

    public Budget() {
    }

    public Long getId() {
        return id;
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public BigDecimal getTotalBudget() {
        return totalBudget;
    }

    public void setTotalBudget(BigDecimal totalBudget) {
        this.totalBudget = totalBudget;
    }

    public BigDecimal getAccommodationBudget() {
        return accommodationBudget;
    }

    public void setAccommodationBudget(BigDecimal accommodationBudget) {
        this.accommodationBudget = accommodationBudget;
    }

    public BigDecimal getFoodBudget() {
        return foodBudget;
    }

    public void setFoodBudget(BigDecimal foodBudget) {
        this.foodBudget = foodBudget;
    }

    public BigDecimal getTransportBudget() {
        return transportBudget;
    }

    public void setTransportBudget(BigDecimal transportBudget) {
        this.transportBudget = transportBudget;
    }

    public BigDecimal getActivityBudget() {
        return activityBudget;
    }

    public void setActivityBudget(BigDecimal activityBudget) {
        this.activityBudget = activityBudget;
    }

    public BigDecimal getMiscellaneousBudget() {
        return miscellaneousBudget;
    }

    public void setMiscellaneousBudget(BigDecimal miscellaneousBudget) {
        this.miscellaneousBudget = miscellaneousBudget;
    }
}