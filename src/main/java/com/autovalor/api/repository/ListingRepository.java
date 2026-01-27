package com.autovalor.api.repository;

import com.autovalor.api.model.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ListingRepository extends JpaRepository<Listing, Long> {

    @Query("select l from Listing l join fetch l.user")
    List<Listing> findAllWithUser();
}
