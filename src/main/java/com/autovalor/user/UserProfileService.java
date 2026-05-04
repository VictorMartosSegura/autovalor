package com.autovalor.user;

import com.autovalor.api.repository.ContactMessageRepository;
import com.autovalor.api.repository.FavoriteRepository;
import com.autovalor.api.repository.ListingImageRepository;
import com.autovalor.api.repository.ListingRepository;
import com.autovalor.user.dto.ChangeSecretRequest;
import com.autovalor.user.dto.UpdateProfileRequest;
import com.autovalor.user.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserProfileService {

    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final FavoriteRepository favoriteRepository;
    private final ListingImageRepository listingImageRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileService(
            UserRepository userRepository,
            ListingRepository listingRepository,
            FavoriteRepository favoriteRepository,
            ListingImageRepository listingImageRepository,
            ContactMessageRepository contactMessageRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
        this.favoriteRepository = favoriteRepository;
        this.listingImageRepository = listingImageRepository;
        this.contactMessageRepository = contactMessageRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse updateProfile(User user, UpdateProfileRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        userRepository.findByEmail(normalizedEmail)
                .filter(existing -> !existing.getId().equals(user.getId()))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe una cuenta con este email");
                });

        user.setName(request.getName().trim());
        user.setEmail(normalizedEmail);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void changeSecret(User user, ChangeSecretRequest request) {
        if (!passwordEncoder.matches(request.getCurrentSecret(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña actual no es correcta");
        }

        user.setPassword(passwordEncoder.encode(request.getNewSecret()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(User user) {
        if (user.getRole() == Role.ADMIN && userRepository.findAllByRole(Role.ADMIN).size() <= 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes borrar el unico administrador");
        }

        listingRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId()).forEach(listing -> {
            Long listingId = listing.getId();
            contactMessageRepository.deleteAllByListingId(listingId);
            favoriteRepository.deleteAllByListingId(listingId);
            listingImageRepository.deleteAll(listingImageRepository.findAllByListingIdOrderByCreatedAtAsc(listingId));
            listingRepository.delete(listing);
        });

        favoriteRepository.deleteAll(
                favoriteRepository.findAllByUserIdWithListing(user.getId())
        );

        userRepository.delete(user);
    }
}
