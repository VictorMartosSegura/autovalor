package com.autovalor.api.repository;

import com.autovalor.api.model.ContactMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    @Query("select m from ContactMessage m join fetch m.listing l join fetch l.user where l.user.id = :ownerId order by m.createdAt desc")
    List<ContactMessage> findAllByListingOwnerId(Long ownerId);

    @Query("select m from ContactMessage m join fetch m.listing l join fetch l.user where l.id = :listingId order by m.createdAt desc")
    List<ContactMessage> findAllByListingIdWithListing(Long listingId);

    void deleteAllByListingId(Long listingId);
}
