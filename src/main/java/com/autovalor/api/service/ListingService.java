package com.autovalor.api.service;

import com.autovalor.api.dto.listingDTO.CreateListingRequest;
import com.autovalor.api.dto.listingDTO.ListingPageResponse;
import com.autovalor.api.dto.listingDTO.ListingResponse;
import com.autovalor.api.mapper.ListingMapper;
import com.autovalor.api.model.Listing;
import com.autovalor.api.model.ListingStatus;
import com.autovalor.api.repository.ContactMessageRepository;
import com.autovalor.api.repository.FavoriteRepository;
import com.autovalor.api.repository.ListingImageRepository;
import com.autovalor.api.repository.ListingRepository;
import com.autovalor.user.Role;
import com.autovalor.user.User;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ListingService {

    private final ListingRepository listingRepository;
    private final FavoriteRepository favoriteRepository;
    private final ListingImageRepository listingImageRepository;
    private final ContactMessageRepository contactMessageRepository;

    public ListingService(
            ListingRepository listingRepository,
            FavoriteRepository favoriteRepository,
            ListingImageRepository listingImageRepository,
            ContactMessageRepository contactMessageRepository
    ) {
        this.listingRepository = listingRepository;
        this.favoriteRepository = favoriteRepository;
        this.listingImageRepository = listingImageRepository;
        this.contactMessageRepository = contactMessageRepository;
    }

    @Transactional(readOnly = true)
    public List<ListingResponse> findAllPublic() {
        return listingRepository.findAllPublicWithUser().stream()
                .map(ListingMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ListingPageResponse searchPublic(
            String query,
            String brand,
            String model,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer minYear,
            Integer maxYear,
            Integer minKm,
            Integer maxKm,
            String fuelType,
            String transmission,
            String location,
            String province,
            String sellerType,
            String bodyType,
            Integer doors,
            Integer minPowerCv,
            Integer maxPowerCv,
            String environmentalLabel,
            Boolean warranty,
            Boolean financeable,
            ListingStatus status,
            int page,
            int size,
            String sort
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                resolveSort(sort)
        );

        Page<Listing> result = listingRepository.findAll(
                buildSearchSpec(
                        normalize(query),
                        normalize(brand),
                        normalize(model),
                        minPrice,
                        maxPrice,
                        minYear,
                        maxYear,
                        minKm,
                        maxKm,
                        normalize(fuelType),
                        normalize(transmission),
                        normalize(location),
                        normalize(province),
                        normalize(sellerType),
                        normalize(bodyType),
                        doors,
                        minPowerCv,
                        maxPowerCv,
                        normalize(environmentalLabel),
                        warranty,
                        financeable,
                        status
                ),
                pageable
        );

        return new ListingPageResponse(
                result.getContent().stream().map(ListingMapper::toResponse).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast()
        );
    }

    @Transactional(readOnly = true)
    public ListingResponse findById(Long id, User user) {
        Listing listing = getExistingListing(id);
        if (listing.getStatus() == ListingStatus.HIDDEN && !canViewHiddenListing(listing, user)) {
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
        contactMessageRepository.deleteAllByListingId(id);
        favoriteRepository.deleteAllByListingId(id);
        listingImageRepository.deleteAll(listingImageRepository.findAllByListingIdOrderByCreatedAtAsc(id));
        listingRepository.delete(listing);
    }

    private boolean canViewHiddenListing(Listing listing, User user) {
        if (user == null) {
            return false;
        }
        boolean isOwner = listing.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;
        return isOwner || isAdmin;
    }

    private Specification<Listing> buildSearchSpec(
            String query,
            String brand,
            String model,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer minYear,
            Integer maxYear,
            Integer minKm,
            Integer maxKm,
            String fuelType,
            String transmission,
            String location,
            String province,
            String sellerType,
            String bodyType,
            Integer doors,
            Integer minPowerCv,
            Integer maxPowerCv,
            String environmentalLabel,
            Boolean warranty,
            Boolean financeable,
            ListingStatus status
    ) {
        return (root, criteriaQuery, cb) -> {
            if (criteriaQuery != null && !Long.class.equals(criteriaQuery.getResultType())) {
                root.fetch("user");
                criteriaQuery.distinct(true);
            }

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.notEqual(root.get("status"), ListingStatus.HIDDEN));

            if (query != null) {
                String like = "%" + query.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like),
                        cb.like(cb.lower(root.get("brand")), like),
                        cb.like(cb.lower(root.get("model")), like),
                        cb.like(cb.lower(root.get("location")), like),
                        cb.like(cb.lower(root.get("province")), like)
                ));
            }

            addEqualsIgnoreCase(predicates, cb, root.get("brand"), brand);
            addEqualsIgnoreCase(predicates, cb, root.get("model"), model);
            addEqualsIgnoreCase(predicates, cb, root.get("fuelType"), fuelType);
            addEqualsIgnoreCase(predicates, cb, root.get("transmission"), transmission);
            addEqualsIgnoreCase(predicates, cb, root.get("location"), location);
            addEqualsIgnoreCase(predicates, cb, root.get("province"), province);
            addEqualsIgnoreCase(predicates, cb, root.get("sellerType"), sellerType);
            addEqualsIgnoreCase(predicates, cb, root.get("bodyType"), bodyType);
            addEqualsIgnoreCase(predicates, cb, root.get("environmentalLabel"), environmentalLabel);

            if (minPrice != null) predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            if (maxPrice != null) predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            if (minYear != null) predicates.add(cb.greaterThanOrEqualTo(root.get("year"), minYear));
            if (maxYear != null) predicates.add(cb.lessThanOrEqualTo(root.get("year"), maxYear));
            if (minKm != null) predicates.add(cb.greaterThanOrEqualTo(root.get("km"), minKm));
            if (maxKm != null) predicates.add(cb.lessThanOrEqualTo(root.get("km"), maxKm));
            if (doors != null) predicates.add(cb.equal(root.get("doors"), doors));
            if (minPowerCv != null) predicates.add(cb.greaterThanOrEqualTo(root.get("powerCv"), minPowerCv));
            if (maxPowerCv != null) predicates.add(cb.lessThanOrEqualTo(root.get("powerCv"), maxPowerCv));
            if (warranty != null) predicates.add(cb.equal(root.get("warranty"), warranty));
            if (financeable != null) predicates.add(cb.equal(root.get("financeable"), financeable));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void addEqualsIgnoreCase(
            List<Predicate> predicates,
            jakarta.persistence.criteria.CriteriaBuilder cb,
            jakarta.persistence.criteria.Path<String> path,
            String value
    ) {
        if (value != null) {
            predicates.add(cb.equal(cb.lower(path), value.toLowerCase()));
        }
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

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private Sort resolveSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        return switch (sort) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "year_asc" -> Sort.by(Sort.Direction.ASC, "year");
            case "year_desc" -> Sort.by(Sort.Direction.DESC, "year");
            case "km_asc" -> Sort.by(Sort.Direction.ASC, "km");
            case "km_desc" -> Sort.by(Sort.Direction.DESC, "km");
            case "power_asc" -> Sort.by(Sort.Direction.ASC, "powerCv");
            case "power_desc" -> Sort.by(Sort.Direction.DESC, "powerCv");
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
}
