package com.tripnest.tripnest_backend.dto;

import com.tripnest.tripnest_backend.entity.Attraction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttractionResponse {

    private Long id;
    private String name;
    private String shortDescription;
    private Integer destinationId;

    public static AttractionResponse fromEntity(Attraction attraction) {
        if (attraction == null) {
            return null;
        }

        Integer destId = attraction.getDestination() != null ? attraction.getDestination().getId() : null;

        return AttractionResponse.builder()
                .id(attraction.getId())
                .name(attraction.getName())
                .shortDescription(attraction.getShortDescription())
                .destinationId(destId)
                .build();
    }
}
