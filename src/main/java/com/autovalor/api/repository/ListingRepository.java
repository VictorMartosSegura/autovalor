package com.autovalor.api.repository;

import com.autovalor.api.model.Listing;
import com.autovalor.api.model.ListingStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ListingRepository extends JpaRepository<Listing, Long> {

    @Query("select l from Listing l join fetch l.user order by l.createdAt desc")
    List<Listing> findAllWithUser();

    @Query("select l from Listing l join fetch l.user where l.status <> 'HIDDEN' order by l.createdAt desc")
    List<Listing> findAllPublicWithUser();

    List<Listing> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    List<Listing> findAllByStatusOrderByCreatedAtDesc(ListingStatus status);
}
