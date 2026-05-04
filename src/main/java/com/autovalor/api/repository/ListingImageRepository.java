package com.autovalor.api.repository;

import com.autovalor.api.model.ListingImage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListingImageRepository extends JpaRepository<ListingImage, Long> {
    List<ListingImage> findAllByListingIdOrderByCreatedAtAsc(Long listingId);

    Optional<ListingImage> findByIdAndListingId(Long id, Long listingId);

    long countByListingId(Long listingId);
}
