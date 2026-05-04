package com.autovalor.api.service;

import com.autovalor.api.dto.listingDTO.CreateListingRequest;
import com.autovalor.api.dto.listingDTO.ListingResponse;
import com.autovalor.api.mapper.ListingMapper;
import com.autovalor.api.model.Listing;
import com.autovalor.api.model.ListingStatus;
import com.autovalor.api.repository.FavoriteRepository;
import com.autovalor.api.repository.ListingImageRepository;
import com.autovalor.api.repository.ListingRepository;
import com.autovalor.user.Role;
import com.autovalor.user.User;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ListingService {

    private final ListingRepository listingRepository;
    private final FavoriteRepository favoriteRepository;
    private final ListingImageRepository listingImageRepository;

    public ListingService(
            ListingRepository listingRepository,
            FavoriteRepository favoriteRepository,
            ListingImageRepository listingImageRepository
    ) {
        this.listingRepository = listingRepository;
        this.favoriteRepository = favoriteRepository;
        this.listingImageRepository = listingImageRepository;
    }

    @Transactional(readOnly = true)
    public List<ListingResponse> findAllPublic() {
        return listingRepository.findAllPublicWithUser().stream()
                .map(ListingMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ListingResponse findById(Long id) {
        Listing listing = getExistingListing(id);
        if (listing.getStatus() == ListingStatus.HIDDEN) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Anuncio no encontrado");
        }
        return ListingMapper.toResponse(listing);
    }

    @Transactional(readOnly = true)
    public List<ListingResponse> findMine(User user) {
        return listingRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(ListingMapper::toResponse)
                .toList();
    }

    @Transactional
    public ListingResponse create(CreateListingRequest request, User owner) {
        Listing listing = ListingMapper.toEntity(request, owner);
        listing.setStatus(ListingStatus.AVAILABLE);
        return ListingMapper.toResponse(listingRepository.save(listing));
    }

    @Transactional
    public ListingResponse update(Long id, CreateListingRequest request, User user) {
        Listing listing = getExistingListing(id);
        assertOwnerOrAdmin(listing, user);
        ListingMapper.updateEntity(listing, request);
        return ListingMapper.toResponse(listingRepository.save(listing));
    }

    @Transactional
    public ListingResponse updateStatus(Long id, ListingStatus status, User user) {
        Listing listing = getExistingListing(id);
        assertOwnerOrAdmin(listing, user);
        listing.setStatus(status);
        listing.markUpdated();
        return ListingMapper.toResponse(listingRepository.save(listing));
    }

    @Transactional
    public void delete(Long id, User user) {
        Listing listing = getExistingListing(id);
        assertOwnerOrAdmin(listing, user);
        favoriteRepository.deleteAllByListingId(id);
        listingImageRepository.deleteAll(listingImageRepository.findAllByListingIdOrderByCreatedAtAsc(id));
        listingRepository.delete(listing);
    }

    private Listing getExistingListing(Long id) {
        return listingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anuncio no encontrado"));
    }

    private void assertOwnerOrAdmin(Listing listing, User user) {
        boolean isOwner = listing.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes modificar este anuncio");
        }
    }
}
