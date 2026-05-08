package com.autovalor.api.repository;

import com.autovalor.api.model.ChatMessage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("""
            select m from ChatMessage m
            join fetch m.sender
            where m.conversation.id = :conversationId
            order by m.createdAt asc
            """)
    List<ChatMessage> findAllByConversationId(@Param("conversationId") Long conversationId);

    @Query("""
            select m from ChatMessage m
            join fetch m.sender
            where m.conversation.id = :conversationId
            order by m.createdAt desc
            limit 1
            """)
    Optional<ChatMessage> findLastByConversationId(@Param("conversationId") Long conversationId);

    long countByConversationIdAndSenderIdNotAndReadAtIsNull(Long conversationId, Long senderId);
}
