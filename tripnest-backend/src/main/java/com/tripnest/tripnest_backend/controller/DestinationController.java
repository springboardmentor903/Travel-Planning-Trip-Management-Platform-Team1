package com.tripnest.tripnest_backend.controller;

import com.tripnest.tripnest_backend.entity.Destination;
import com.tripnest.tripnest_backend.service.DestinationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/destinations")
public class DestinationController {

    private final DestinationService destinationService;

    public DestinationController(DestinationService destinationService) {
        this.destinationService = destinationService;
    }

    @GetMapping
    public List<Destination> getAllDestinations() {
        return destinationService.getAllDestinations();
    }

    @GetMapping("/{id}")
    public Destination getDestinationById(@PathVariable Integer id) {
        return destinationService.getDestinationById(id);
    }
}