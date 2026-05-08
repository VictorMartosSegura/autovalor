package com.autovalor.api.service;

import com.autovalor.api.dto.chatDTO.ConversationResponse;
import com.autovalor.api.dto.chatDTO.MessageResponse;
import com.autovalor.api.dto.chatDTO.SendMessageRequest;
import com.autovalor.api.model.ChatMessage;
import com.autovalor.api.model.Conversation;
import com.autovalor.api.model.Listing;
import com.autovalor.api.repository.ChatMessageRepository;
import com.autovalor.api.repository.ConversationRepository;
import com.autovalor.api.repository.ListingRepository;
import com.autovalor.user.User;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ListingRepository listingRepository;

    public ConversationService(
            ConversationRepository conversationRepository,
            ChatMessageRepository chatMessageRepository,
            ListingRepository listingRepository
    ) {
        this.conversationRepository = conversationRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.listingRepository = listingRepository;
    }

    @Transactional
    public ConversationResponse startForListing(Long listingId, User currentUser) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));

        User seller = listing.getUser();
        if (seller.getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot start a conversation with yourself");
        }

        Conversation conversation = conversationRepository
                .findByListingIdAndBuyerIdAndSellerId(listing.getId(), currentUser.getId(), seller.getId())
                .orElseGet(() -> conversationRepository.save(new Conversation(listing, currentUser, seller)));

        return toConversationResponse(conversation, currentUser);
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> findMine(User currentUser) {
        return conversationRepository.findAllForUser(currentUser.getId())
                .stream()
                .map(conversation -> toConversationResponse(conversation, currentUser))
                .toList();
    }

    @Transactional(readOnly = true)
    public ConversationResponse findOne(Long conversationId, User currentUser) {
        Conversation conversation = getConversationForUser(conversationId, currentUser);
        return toConversationResponse(conversation, currentUser);
    }

    @Transactional
    public List<MessageResponse> findMessages(Long conversationId, User currentUser) {
        Conversation conversation = getConversationForUser(conversationId, currentUser);
        List<ChatMessage> messages = chatMessageRepository.findAllByConversationId(conversation.getId());

        messages.stream()
                .filter(message -> !message.getSender().getId().equals(currentUser.getId()))
                .filter(message -> message.getReadAt() == null)
                .forEach(ChatMessage::markRead);

        return messages.stream()
                .map(message -> toMessageResponse(message, currentUser))
                .toList();
    }

    @Transactional
    public MessageResponse sendMessage(Long conversationId, SendMessageRequest request, User currentUser) {
        Conversation conversation = getConversationForUser(conversationId, currentUser);
        String content = request.content() == null ? "" : request.content().trim();
        if (content.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message content is required");
        }

        ChatMessage message = chatMessageRepository.save(new ChatMessage(conversation, currentUser, content));
        conversation.markUpdated();
        return toMessageResponse(message, currentUser);
    }

    private Conversation getConversationForUser(Long conversationId, User currentUser) {
        return conversationRepository.findForUser(conversationId, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
    }

    private ConversationResponse toConversationResponse(Conversation conversation, User currentUser) {
        User otherUser = conversation.otherParticipant(currentUser);
        ChatMessage lastMessage = chatMessageRepository
                .findTopByConversationIdOrderByCreatedAtDesc(conversation.getId())
                .orElse(null);
        long unreadCount = chatMessageRepository
                .countByConversationIdAndSenderIdNotAndReadAtIsNull(conversation.getId(), currentUser.getId());

        OffsetDateTime lastMessageAt = lastMessage == null ? null : lastMessage.getCreatedAt();
        String lastMessageText = lastMessage == null ? null : lastMessage.getContent();

        return new ConversationResponse(
                conversation.getId(),
                conversation.getListing().getId(),
                conversation.getListing().getTitle(),
                otherUser.getId(),
                otherUser.getName(),
                otherUser.getEmail(),
                lastMessageText,
                lastMessageAt,
                unreadCount,
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
    }

    private MessageResponse toMessageResponse(ChatMessage message, User currentUser) {
        return new MessageResponse(
                message.getId(),
                message.getConversation().getId(),
                message.getSender().getId(),
                message.getSender().getName(),
                message.getContent(),
                message.getCreatedAt(),
                message.getReadAt(),
                message.getSender().getId().equals(currentUser.getId())
        );
    }
}
