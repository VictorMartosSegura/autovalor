package com.autovalor.api.controller.rest;

import com.autovalor.api.dto.chatDTO.ConversationResponse;
import com.autovalor.api.dto.chatDTO.MessageResponse;
import com.autovalor.api.dto.chatDTO.SendMessageRequest;
import com.autovalor.api.service.ConversationService;
import com.autovalor.user.User;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping("/listing/{listingId}")
    public ResponseEntity<ConversationResponse> startForListing(
            @PathVariable Long listingId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(conversationService.startForListing(listingId, user));
    }

    @GetMapping
    public List<ConversationResponse> findMine(@AuthenticationPrincipal User user) {
        return conversationService.findMine(user);
    }

    @GetMapping("/{conversationId}")
    public ConversationResponse findOne(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal User user
    ) {
        return conversationService.findOne(conversationId, user);
    }

    @GetMapping("/{conversationId}/messages")
    public List<MessageResponse> findMessages(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal User user
    ) {
        return conversationService.findMessages(conversationId, user);
    }

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<MessageResponse> sendMessage(
            @PathVariable Long conversationId,
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(conversationService.sendMessage(conversationId, request, user));
    }
}
