// package com.tripnest.tripnest_backend.controller;

// import com.tripnest.tripnest_backend.entity.User;
// import com.tripnest.tripnest_backend.repository.UserRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.core.Authentication;
// import org.springframework.web.bind.annotation.*;

// import java.util.Map;

// @RestController
// @RequestMapping("/api/users")
// @RequiredArgsConstructor
// public class UserController {

//     private final UserRepository userRepository;

//     // ================================
//     // GET CURRENT USER PROFILE
//     // ================================
//     @GetMapping("/profile")
//     public ResponseEntity<?> getProfile(Authentication authentication) {

//         if (authentication == null || authentication.getName() == null) {
//             return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                     .body(Map.of("message", "Authentication required"));
//         }

//         String email = authentication.getName();

//         User user = userRepository.findByEmail(email)
//                 .orElseThrow(() ->
//                         new RuntimeException("User not found"));

//         return ResponseEntity.ok(Map.of(
//                 "id", user.getId(),
//                 "name", user.getName(),
//                 "email", user.getEmail(),
//                 "oauthGoogle", user.getOauthGoogle(),
//                 "createdAt", user.getCreatedAt()
//         ));
//     }
// }



package com.tripnest.tripnest_backend.controller;

import com.tripnest.tripnest_backend.entity.Destination;
import com.tripnest.tripnest_backend.entity.TravelPreference;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.DestinationRepository;
import com.tripnest.tripnest_backend.repository.TravelPreferenceRepository;
import com.tripnest.tripnest_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final TravelPreferenceRepository travelPreferenceRepository;
    private final DestinationRepository destinationRepository;


    // ==========================================
    // GET CURRENT USER PROFILE
    // ==========================================

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {

        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication required"));
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole() != null ? user.getRole().getName() : "TRAVELER",
                "oauthGoogle", user.getOauthGoogle(),
                "createdAt", user.getCreatedAt()
        ));
    }



    // ==========================================
// UPDATE CURRENT USER PROFILE
// ==========================================

@PutMapping("/profile")
public ResponseEntity<?> updateProfile(
        Authentication authentication,
        @RequestBody Map<String, Object> request) {

    if (authentication == null || authentication.getName() == null) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Authentication required"));
    }

    String email = authentication.getName();

    User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                    new RuntimeException("User not found"));

    // Update name only
    if (request.containsKey("name")) {

        Object nameObject = request.get("name");

        if (nameObject == null || nameObject.toString().trim().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Name cannot be empty"));
        }

        user.setName(nameObject.toString().trim());
    }

    User updatedUser = userRepository.save(user);

    return ResponseEntity.ok(Map.of(
            "message", "Profile updated successfully",
            "id", updatedUser.getId(),
            "name", updatedUser.getName(),
            "email", updatedUser.getEmail(),
            "oauthGoogle", updatedUser.getOauthGoogle(),
            "createdAt", updatedUser.getCreatedAt()
    ));
}


    // ==========================================
    // GET CURRENT USER TRAVEL PREFERENCES
    // ==========================================

    @GetMapping("/preferences")
    public ResponseEntity<?> getPreferences(Authentication authentication) {

        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication required"));
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Optional<TravelPreference> preferenceOptional =
                travelPreferenceRepository.findByUserId(user.getId());

        // If user has no preferences yet
        if (preferenceOptional.isEmpty()) {

    Map<String, Object> emptyPreferences = new java.util.HashMap<>();

    emptyPreferences.put("preferredTravelType", "");
    emptyPreferences.put("preferredDestination", null);
    emptyPreferences.put("favouriteDestination", null);

    return ResponseEntity.ok(emptyPreferences);
}

        TravelPreference preference = preferenceOptional.get();

        return ResponseEntity.ok(Map.of(
                "preferredTravelType",
                preference.getPreferredTravelType() == null
                        ? ""
                        : preference.getPreferredTravelType(),

                "preferredDestination",
                preference.getPreferredDestination() == null
                        ? null
                        : Map.of(
                                "id", preference.getPreferredDestination().getId(),
                                "name", preference.getPreferredDestination().getName()
                        ),

                "favouriteDestination",
                preference.getFavouriteDestination() == null
                        ? null
                        : Map.of(
                                "id", preference.getFavouriteDestination().getId(),
                                "name", preference.getFavouriteDestination().getName()
                        )
        ));
    }


    // ==========================================
    // UPDATE CURRENT USER TRAVEL PREFERENCES
    // ==========================================

    @PutMapping("/preferences")
    public ResponseEntity<?> updatePreferences(
            Authentication authentication,
            @RequestBody Map<String, Object> request) {

        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication required"));
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));


        TravelPreference preference =
                travelPreferenceRepository
                        .findByUserId(user.getId())
                        .orElseGet(() -> {

                            TravelPreference newPreference =
                                    new TravelPreference();

                            newPreference.setUser(user);

                            return newPreference;
                        });


        // Preferred travel type
        if (request.containsKey("preferredTravelType")) {

            Object travelType =
                    request.get("preferredTravelType");

            preference.setPreferredTravelType(
                    travelType == null
                            ? null
                            : travelType.toString()
            );
        }


        // Preferred destination
        if (request.containsKey("preferredDestinationId")) {

            Object destinationId =
                    request.get("preferredDestinationId");

            if (destinationId != null) {

                Integer id =
                        Integer.valueOf(destinationId.toString());

                Destination destination =
                        destinationRepository.findById(id)
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Preferred destination not found"
                                        ));

                preference.setPreferredDestination(destination);

            } else {

                preference.setPreferredDestination(null);
            }
        }


        // Favourite destination
        if (request.containsKey("favouriteDestinationId")) {

            Object destinationId =
                    request.get("favouriteDestinationId");

            if (destinationId != null) {

                Integer id =
                        Integer.valueOf(destinationId.toString());

                Destination destination =
                        destinationRepository.findById(id)
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Favourite destination not found"
                                        ));

                preference.setFavouriteDestination(destination);

            } else {

                preference.setFavouriteDestination(null);
            }
        }


        TravelPreference savedPreference =
                travelPreferenceRepository.save(preference);


        return ResponseEntity.ok(Map.of(
                "message", "Travel preferences updated successfully",
                "preferredTravelType",
                savedPreference.getPreferredTravelType() == null
                        ? ""
                        : savedPreference.getPreferredTravelType(),

                "preferredDestination",
                savedPreference.getPreferredDestination() == null
                        ? "Not selected"
                        : savedPreference
                                .getPreferredDestination()
                                .getName(),

                "favouriteDestination",
                savedPreference.getFavouriteDestination() == null
                        ? "Not selected"
                        : savedPreference
                                .getFavouriteDestination()
                                .getName()
        ));
    }
}