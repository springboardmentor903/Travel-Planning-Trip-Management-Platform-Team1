package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.entity.Destination;
import com.tripnest.tripnest_backend.repository.DestinationRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DestinationService {

        private final DestinationRepository destinationRepository;
        private final RestTemplate restTemplate;

        @Value("${google.places.api.key}")
        private String googlePlacesApiKey;

        public DestinationService(
                        DestinationRepository destinationRepository,
                        RestTemplate restTemplate) {

                this.destinationRepository = destinationRepository;
                this.restTemplate = restTemplate;
        }

        // ==========================================
        // GET ALL DESTINATIONS
        // ==========================================

        public List<Destination> getAllDestinations() {
                return destinationRepository.findAll();
        }

        // ==========================================
        // GET DESTINATION BY ID
        // ==========================================

        public Destination getDestinationById(Integer id) {

                return destinationRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Destination not found"));
        }

        // ==========================================
        // GOOGLE PLACES SEARCH
        // ==========================================

        public Object searchGooglePlaces(String query) {

                String url = "https://places.googleapis.com/v1/places:searchText";

                HttpHeaders headers = new HttpHeaders();

                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("X-Goog-Api-Key", googlePlacesApiKey);

                headers.set(
                                "X-Goog-FieldMask",
                                "places.id,"
                                                + "places.displayName,"
                                                + "places.formattedAddress,"
                                                + "places.location,"
                                                + "places.types,"
                                                + "places.rating,"
                                                + "places.userRatingCount,"
                                                + "places.websiteUri");

                Map<String, Object> requestBody = new HashMap<>();

                requestBody.put("textQuery", query);
                requestBody.put("pageSize", 10);

                HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

                ResponseEntity<Object> response = restTemplate.exchange(
                                url,
                                HttpMethod.POST,
                                requestEntity,
                                Object.class);

                return response.getBody();
        }

        // ==========================================
        // GOOGLE PLACE DETAILS
        // ==========================================

        public Object getGooglePlaceDetails(String placeId) {

                String url = "https://places.googleapis.com/v1/places/"
                                + placeId;

                HttpHeaders headers = new HttpHeaders();

                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("X-Goog-Api-Key", googlePlacesApiKey);

                headers.set(
                                "X-Goog-FieldMask",
                                "places.id,"
                                                + "places.displayName,"
                                                + "places.formattedAddress,"
                                                + "places.location,"
                                                + "places.types,"
                                                + "places.rating,"
                                                + "places.userRatingCount,"
                                                + "places.websiteUri,"
                                                + "places.photos");

                HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

                ResponseEntity<Object> response = restTemplate.exchange(
                                url,
                                HttpMethod.GET,
                                requestEntity,
                                Object.class);

                return response.getBody();
        }

        // ==========================================
        // SEARCH DESTINATION
        // ==========================================

        public Object searchDestination(String name) {
                return searchGooglePlaces(name);
        }

        // ==========================================
        // CREATE OR GET DESTINATION
        // ==========================================

        public Destination createOrGetDestination(String name) {

                if (name == null || name.trim().isEmpty()) {
                        throw new RuntimeException("Destination name is required");
                }

                String destinationName = name.trim();

                return destinationRepository
                                .findByNameIgnoreCase(destinationName)
                                .orElseGet(() -> {

                                        Destination destination = new Destination();

                                        destination.setName(destinationName);

                                        return destinationRepository.save(destination);
                                });
        }



        public ResponseEntity<byte[]> getGooglePlacePhoto(String photoName) {

    String url =
            "https://places.googleapis.com/v1/"
                    + photoName
                    + "/media?maxWidthPx=600&maxHeightPx=400";

    HttpHeaders headers = new HttpHeaders();

    headers.set("X-Goog-Api-Key", googlePlacesApiKey);

    ResponseEntity<byte[]> response =
            restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    byte[].class
            );

    HttpHeaders responseHeaders = new HttpHeaders();

    MediaType contentType =
            response.getHeaders().getContentType();

    if (contentType != null) {
        responseHeaders.setContentType(contentType);
    } else {
        responseHeaders.setContentType(MediaType.IMAGE_JPEG);
    }

    return new ResponseEntity<>(
            response.getBody(),
            responseHeaders,
            response.getStatusCode()
    );
}
}