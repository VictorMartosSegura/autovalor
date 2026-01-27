package com.autovalor.api.mapper;

import com.autovalor.api.dto.listingDTO.CreateListingRequest;
import com.autovalor.api.dto.listingDTO.ListingResponse;
import com.autovalor.api.model.Listing;
import com.autovalor.api.model.User;

public final class ListingMapper {

    private ListingMapper() {}

    /** Request -> Entity (para crear) */
    public static Listing toEntity(CreateListingRequest req, User owner) {
        if (req == null) throw new IllegalArgumentException("CreateListingRequest is null");
        if (owner == null) throw new IllegalArgumentException("Owner(User) is null");

        Listing l = new Listing();
        l.setUser(owner);

        l.setTitle(req.getTitle());
        l.setDescription(req.getDescription());
        l.setPrice(req.getPrice());

        l.setBrand(req.getBrand());
        l.setModel(req.getModel());
        l.setYear(req.getYear());
        l.setKm(req.getKm());

        return l;
    }

    /** Entity -> Response DTO (seguro para API) */
    public static ListingResponse toResponse(Listing l) {
        if (l == null) return null;

        ListingResponse dto = new ListingResponse();
        dto.setId(l.getId());
        dto.setTitle(l.getTitle());
        dto.setDescription(l.getDescription());
        dto.setPrice(l.getPrice());

        dto.setBrand(l.getBrand());
        dto.setModel(l.getModel());
        dto.setYear(l.getYear());
        dto.setKm(l.getKm());

        dto.setCreatedAt(l.getCreatedAt());

        // Solo info mínima del owner (nada de email, nada de passwordHash)
        User u = l.getUser();
        if (u != null) {
            dto.setUserId(u.getId());
            dto.setUserName(u.getName());
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
