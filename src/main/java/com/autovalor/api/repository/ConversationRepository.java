package com.autovalor.api.repository;

import com.autovalor.api.model.Conversation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByListingIdAndBuyerIdAndSellerId(Long listingId, Long buyerId, Long sellerId);

    @Query("""
            select c from Conversation c
            join fetch c.listing l
            join fetch c.buyer b
            join fetch c.seller s
            where b.id = :userId or s.id = :userId
            order by c.updatedAt desc
            """)
    List<Conversation> findAllForUser(@Param("userId") Long userId);

    @Query("""
            select c from Conversation c
            join fetch c.listing l
            join fetch c.buyer b
            join fetch c.seller s
            where c.id = :id and (b.id = :userId or s.id = :userId)
            """)
    Optional<Conversation> findForUser(@Param("id") Long id, @Param("userId") Long userId);
}
