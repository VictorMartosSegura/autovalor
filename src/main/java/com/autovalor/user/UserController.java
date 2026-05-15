package com.autovalor.user;

import com.autovalor.api.dto.favoriteDTO.FavoriteResponse;
import com.autovalor.api.dto.listingDTO.ListingResponse;
import com.autovalor.api.service.FavoriteService;
import com.autovalor.api.service.ListingService;
import com.autovalor.user.dto.ChangeSecretRequest;
import com.autovalor.user.dto.UpdateProfileRequest;
import com.autovalor.user.dto.UserAddressRequest;
import com.autovalor.user.dto.UserAddressResponse;
import com.autovalor.user.dto.UserResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserProfileService userProfileService;
    private final ListingService listingService;
    private final FavoriteService favoriteService;

    public UserController(UserProfileService userProfileService, ListingService listingService, FavoriteService favoriteService) {
        this.userProfileService = userProfileService;
        this.listingService = listingService;
        this.favoriteService = favoriteService;
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(@AuthenticationPrincipal User user) {
        return UserResponse.from(user);
    }

    @PutMapping("/me")
    public UserResponse updateCurrentUser(@AuthenticationPrincipal User user, @Valid @RequestBody UpdateProfileRequest request) {
        return userProfileService.updateProfile(user, request);
    }

    @GetMapping("/me/address")
    public UserAddressResponse getCurrentUserAddress(@AuthenticationPrincipal User user) {
        return UserAddressResponse.from(user);
    }

    @PutMapping("/me/address")
    public UserAddressResponse updateCurrentUserAddress(@AuthenticationPrincipal User user, @Valid @RequestBody UserAddressRequest request) {
        return userProfileService.updateAddress(user, request);
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> changeCurrentUserPassword(@AuthenticationPrincipal User user, @Valid @RequestBody ChangeSecretRequest request) {
        userProfileService.changeSecret(user, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser(@AuthenticationPrincipal User user) {
        userProfileService.deleteAccount(user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/listings")
    public List<ListingResponse> getMyListings(@AuthenticationPrincipal User user) {
        return listingService.findMine(user);
    }

    @GetMapping("/me/favorites")
    public List<FavoriteResponse> getMyFavorites(@AuthenticationPrincipal User user) {
        return favoriteService.findMine(user);
    }
}
