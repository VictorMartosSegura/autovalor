package com.autovalor.api.repository;

import com.autovalor.api.model.Favorite;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserIdAndListingId(Long userId, Long listingId);

    Optional<Favorite> findByUserIdAndListingId(Long userId, Long listingId);

    long countByListingId(Long listingId);

    @Query("select f from Favorite f join fetch f.listing l join fetch l.user where f.user.id = :userId order by f.createdAt desc")
    List<Favorite> findAllByUserIdWithListing(Long userId);

    void deleteAllByListingId(Long listingId);
}
