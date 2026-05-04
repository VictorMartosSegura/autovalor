package com.autovalor.api.mapper;

import com.autovalor.api.dto.listingDTO.CreateListingRequest;
import com.autovalor.api.dto.listingDTO.ListingResponse;
import com.autovalor.api.model.Listing;
import com.autovalor.user.User;

public final class ListingMapper {

    private ListingMapper() {
    }

    /** Request -> Entity (para crear) */
    public static Listing toEntity(CreateListingRequest req, User owner) {
        if (req == null) throw new IllegalArgumentException("CreateListingRequest is null");
        if (owner == null) throw new IllegalArgumentException("Owner(User) is null");

        Listing listing = new Listing();
        listing.setUser(owner);

        listing.setTitle(req.getTitle());
        listing.setDescription(req.getDescription());
        listing.setPrice(req.getPrice());

        listing.setBrand(req.getBrand());
        listing.setModel(req.getModel());
        listing.setYear(req.getYear());
        listing.setKm(req.getKm());

        return listing;
    }

    /** Entity -> Response DTO (seguro para API) */
    public static ListingResponse toResponse(Listing listing) {
        if (listing == null) return null;

        ListingResponse dto = new ListingResponse();
        dto.setId(listing.getId());
        dto.setTitle(listing.getTitle());
        dto.setDescription(listing.getDescription());
        dto.setPrice(listing.getPrice());

        dto.setBrand(listing.getBrand());
        dto.setModel(listing.getModel());
        dto.setYear(listing.getYear());
        dto.setKm(listing.getKm());

        dto.setCreatedAt(listing.getCreatedAt());

        // Solo info minima del owner (nada de email, nada de password)
        User owner = listing.getUser();
        if (owner != null) {
            dto.setUserId(owner.getId());
            dto.setUserName(owner.getName());
        }

        return dto;
    }

    /** Update (si luego haces PUT/PATCH). No permite cambiar user. */
    public static void updateEntity(Listing target, CreateListingRequest req) {
        if (target == null) throw new IllegalArgumentException("Target Listing is null");
        if (req == null) throw new IllegalArgumentException("CreateListingRequest is null");

        target.setTitle(req.getTitle());
        target.setDescription(req.getDescription());
        target.setPrice(req.getPrice());

        target.setBrand(req.getBrand());
        target.setModel(req.getModel());
        target.setYear(req.getYear());
        target.setKm(req.getKm());
    }
}
