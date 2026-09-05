package com.tripnest.tripnest_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttractionRequest {

    @NotBlank(message = "Attraction name is required")
    @Size(max = 255, message = "Attraction name must not exceed 255 characters")
    private String name;

    @Size(max = 1000, message = "Short description must not exceed 1000 characters")
    private String shortDescription;
}
