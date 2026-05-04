package com.autovalor.api.service;

import com.autovalor.api.dto.contactDTO.ContactMessageResponse;
import com.autovalor.api.dto.contactDTO.CreateContactMessageRequest;
import com.autovalor.api.model.ContactMessage;
import com.autovalor.api.model.Listing;
import com.autovalor.api.model.ListingStatus;
import com.autovalor.api.repository.ContactMessageRepository;
import com.autovalor.api.repository.ListingRepository;
import com.autovalor.user.Role;
import com.autovalor.user.User;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;
    private final ListingRepository listingRepository;

    public ContactMessageService(ContactMessageRepository contactMessageRepository, ListingRepository listingRepository) {
        this.contactMessageRepository = contactMessageRepository;
        this.listingRepository = listingRepository;
    }

    @Transactional
    public ContactMessageResponse create(Long listingId, CreateContactMessageRequest request, User senderUser) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anuncio no encontrado"));

        if (listing.getStatus() == ListingStatus.HIDDEN) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Anuncio no encontrado");
        }

        if (senderUser != null && listing.getUser().getId().equals(senderUser.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes contactar contigo mismo");
        }

        ContactMessage contactMessage = new ContactMessage(
                listing,
                senderUser,
                request.getContactName(),
                request.getContactEmail(),
                request.getContactPhone(),
                request.getMessage()
        );

        return ContactMessageResponse.from(contactMessageRepository.save(contactMessage));
    }

    @Transactional(readOnly = true)
    public List<ContactMessageResponse> findInbox(User user) {
        if (user.getRole() == Role.ADMIN) {
            return contactMessageRepository.findAll().stream()
                    .map(ContactMessageResponse::from)
                    .toList();
        }

        return contactMessageRepository.findAllByListingOwnerId(user.getId()).stream()
                .map(ContactMessageResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ContactMessageResponse> findByListing(Long listingId, User user) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anuncio no encontrado"));

        boolean isOwner = listing.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes ver los mensajes de este anuncio");
        }

        return contactMessageRepository.findAllByListingIdWithListing(listingId).stream()
                .map(ContactMessageResponse::from)
                .toList();
    }
}
