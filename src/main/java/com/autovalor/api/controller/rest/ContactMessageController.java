package com.autovalor.api.controller.rest;

import com.autovalor.api.dto.contactDTO.ContactMessageResponse;
import com.autovalor.api.dto.contactDTO.CreateContactMessageRequest;
import com.autovalor.api.service.ContactMessageService;
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
@RequestMapping("/api")
public class ContactMessageController {

    private final ContactMessageService contactMessageService;

    public ContactMessageController(ContactMessageService contactMessageService) {
        this.contactMessageService = contactMessageService;
    }

    @PostMapping("/cars/{listingId}/contact")
    public ResponseEntity<ContactMessageResponse> createContactMessage(
            @PathVariable Long listingId,
            @Valid @RequestBody CreateContactMessageRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contactMessageService.create(listingId, request, user));
    }

    @PostMapping("/v1/listings/{listingId}/contact")
    public ResponseEntity<ContactMessageResponse> createContactMessageLegacy(
            @PathVariable Long listingId,
            @Valid @RequestBody CreateContactMessageRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contactMessageService.create(listingId, request, user));
    }

    @GetMapping("/contact-messages")
    public List<ContactMessageResponse> getInbox(@AuthenticationPrincipal User user) {
        return contactMessageService.findInbox(user);
    }

    @GetMapping("/cars/{listingId}/contact-messages")
    public List<ContactMessageResponse> getListingMessages(
            @PathVariable Long listingId,
            @AuthenticationPrincipal User user
    ) {
        return contactMessageService.findByListing(listingId, user);
    }
}
