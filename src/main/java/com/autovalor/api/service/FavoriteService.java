package com.autovalor.api.service;

import com.autovalor.api.dto.favoriteDTO.FavoriteResponse;
import com.autovalor.api.dto.favoriteDTO.FavoriteStatusResponse;
import com.autovalor.api.mapper.ListingMapper;
import com.autovalor.api.model.Favorite;
import com.autovalor.api.model.Listing;
import com.autovalor.api.model.ListingStatus;
import com.autovalor.api.repository.FavoriteRepository;
import com.autovalor.api.repository.ListingRepository;
import com.autovalor.user.User;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ListingRepository listingRepository;

    public FavoriteService(FavoriteRepository favoriteRepository, ListingRepository listingRepository) {
        this.favoriteRepository = favoriteRepository;
        this.listingRepository = listingRepository;
    }

    @Transactional(readOnly = true)
    public List<FavoriteResponse> findMine(User user) {
        return favoriteRepository.findAllByUserIdWithListing(user.getId()).stream()
                .filter(favorite -> favorite.getListing().getStatus() != ListingStatus.HIDDEN)
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public FavoriteStatusResponse addFavorite(Long listingId, User user) {
        Listing listing = getPublicListing(listingId);

        if (!favoriteRepository.existsByUserIdAndListingId(user.getId(), listingId)) {
            favoriteRepository.save(new Favorite(user, listing));
        }

        return getStatus(listingId, user);
    }

    @Transactional
    public FavoriteStatusResponse removeFavorite(Long listingId, User user) {
        favoriteRepository.findByUserIdAndListingId(user.getId(), listingId)
                .ifPresent(favoriteRepository::delete);

        return getStatus(listingId, user);
    }

    @Transactional(readOnly = true)
    public FavoriteStatusResponse getStatus(Long listingId, User user) {
        if (!listingRepository.existsById(listingId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Anuncio no encontrado");
        }

        boolean isFavorite = favoriteRepository.existsByUserIdAndListingId(user.getId(), listingId);
        long favoritesCount = favoriteRepository.countByListingId(listingId);
        return new FavoriteStatusResponse(listingId, isFavorite, favoritesCount);
    }

    private Listing getPublicListing(Long listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anuncio no encontrado"));

        if (listing.getStatus() == ListingStatus.HIDDEN) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Anuncio no encontrado");
        }

        return listing;
    }

    private FavoriteResponse toResponse(Favorite favorite) {
        Listing listing = favorite.getListing();
        return new FavoriteResponse(
                favorite.getId(),
                listing.getId(),
                ListingMapper.toResponse(listing),
                favorite.getCreatedAt()
        );
    }
}
