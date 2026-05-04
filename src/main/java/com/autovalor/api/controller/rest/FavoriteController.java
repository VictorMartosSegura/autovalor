package com.autovalor.api.controller.rest;

import com.autovalor.api.dto.favoriteDTO.FavoriteResponse;
import com.autovalor.api.dto.favoriteDTO.FavoriteStatusResponse;
import com.autovalor.api.service.FavoriteService;
import com.autovalor.user.User;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public List<FavoriteResponse> getMyFavorites(@AuthenticationPrincipal User user) {
        return favoriteService.findMine(user);
    }

    @PostMapping("/{listingId}")
    public FavoriteStatusResponse addFavorite(
            @PathVariable Long listingId,
            @AuthenticationPrincipal User user
    ) {
        return favoriteService.addFavorite(listingId, user);
    }

    @DeleteMapping("/{listingId}")
    public FavoriteStatusResponse removeFavorite(
            @PathVariable Long listingId,
            @AuthenticationPrincipal User user
    ) {
        return favoriteService.removeFavorite(listingId, user);
    }

    @GetMapping("/{listingId}/status")
    public FavoriteStatusResponse getFavoriteStatus(
            @PathVariable Long listingId,
            @AuthenticationPrincipal User user
    ) {
        return favoriteService.getStatus(listingId, user);
    }
}
