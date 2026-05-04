package com.autovalor.api.service;

import com.autovalor.api.dto.adminDTO.AdminStatsResponse;
import com.autovalor.api.dto.adminDTO.AdminUserResponse;
import com.autovalor.api.dto.contactDTO.ContactMessageResponse;
import com.autovalor.api.dto.listingDTO.ListingResponse;
import com.autovalor.api.mapper.ListingMapper;
import com.autovalor.api.model.Listing;
import com.autovalor.api.model.ListingStatus;
import com.autovalor.api.repository.ContactMessageRepository;
import com.autovalor.api.repository.FavoriteRepository;
import com.autovalor.api.repository.ListingImageRepository;
import com.autovalor.api.repository.ListingRepository;
import com.autovalor.user.Role;
import com.autovalor.user.User;
import com.autovalor.user.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final FavoriteRepository favoriteRepository;
    private final ListingImageRepository listingImageRepository;
    private final ContactMessageRepository contactMessageRepository;

    public AdminService(
            UserRepository userRepository,
            ListingRepository listingRepository,
            FavoriteRepository favoriteRepository,
            ListingImageRepository listingImageRepository,
            ContactMessageRepository contactMessageRepository
    ) {
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
        this.favoriteRepository = favoriteRepository;
        this.listingImageRepository = listingImageRepository;
        this.contactMessageRepository = contactMessageRepository;
    }

    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        return new AdminStatsResponse(
                userRepository.count(),
                userRepository.findAllByRole(Role.ADMIN).size(),
                listingRepository.count(),
                listingRepository.findAllByStatusOrderByCreatedAtDesc(ListingStatus.AVAILABLE).size(),
                listingRepository.findAllByStatusOrderByCreatedAtDesc(ListingStatus.RESERVED).size(),
                listingRepository.findAllByStatusOrderByCreatedAtDesc(ListingStatus.SOLD).size(),
                listingRepository.findAllByStatusOrderByCreatedAtDesc(ListingStatus.HIDDEN).size(),
                favoriteRepository.count(),
                contactMessageRepository.count()
        );
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> findUsers() {
        return userRepository.findAll().stream()
                .map(AdminUserResponse::from)
                .toList();
    }

    @Transactional
    public AdminUserResponse updateUserRole(Long userId, Role role, User admin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (user.getId().equals(admin.getId()) && role != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes quitarte tu propio rol de administrador");
        }

        user.setRole(role);
        return AdminUserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<ListingResponse> findListings() {
        return listingRepository.findAllWithUser().stream()
                .map(ListingMapper::toResponse)
                .toList();
    }

    @Transactional
    public ListingResponse updateListingStatus(Long listingId, ListingStatus status) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anuncio no encontrado"));

        listing.setStatus(status);
        listing.markUpdated();
        return ListingMapper.toResponse(listingRepository.save(listing));
    }

    @Transactional
    public void deleteListing(Long listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anuncio no encontrado"));

        contactMessageRepository.deleteAllByListingId(listingId);
        favoriteRepository.deleteAllByListingId(listingId);
        listingImageRepository.deleteAll(listingImageRepository.findAllByListingIdOrderByCreatedAtAsc(listingId));
        listingRepository.delete(listing);
    }

    @Transactional(readOnly = true)
    public List<ContactMessageResponse> findContactMessages() {
        return contactMessageRepository.findAll().stream()
                .map(ContactMessageResponse::from)
                .toList();
    }
}
