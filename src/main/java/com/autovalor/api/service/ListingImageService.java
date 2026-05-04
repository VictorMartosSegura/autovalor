package com.autovalor.api.service;

import com.autovalor.api.dto.listingDTO.ListingImageResponse;
import com.autovalor.api.model.Listing;
import com.autovalor.api.model.ListingImage;
import com.autovalor.api.repository.ListingImageRepository;
import com.autovalor.api.repository.ListingRepository;
import com.autovalor.user.Role;
import com.autovalor.user.User;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ListingImageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final ListingRepository listingRepository;
    private final ListingImageRepository listingImageRepository;
    private final Path uploadRoot;
    private final int maxImagesPerListing;

    public ListingImageService(
            ListingRepository listingRepository,
            ListingImageRepository listingImageRepository,
            @Value("${app.upload.dir:uploads}") String uploadDir,
            @Value("${app.upload.max-images-per-listing:6}") int maxImagesPerListing
    ) {
        this.listingRepository = listingRepository;
        this.listingImageRepository = listingImageRepository;
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.maxImagesPerListing = maxImagesPerListing;
    }

    @Transactional(readOnly = true)
    public List<ListingImageResponse> findImages(Long listingId) {
        ensureListingExists(listingId);
        return listingImageRepository.findAllByListingIdOrderByCreatedAtAsc(listingId).stream()
                .map(ListingImageResponse::from)
                .toList();
    }

    @Transactional
    public ListingImageResponse uploadImage(Long listingId, MultipartFile file, User user) {
        Listing listing = getExistingListing(listingId);
        assertOwnerOrAdmin(listing, user);
        validateFile(file);
        validateImageLimit(listingId);

        String extension = getExtension(file.getOriginalFilename(), file.getContentType());
        String fileName = UUID.randomUUID() + extension;
        Path listingDirectory = uploadRoot.resolve("listings").resolve(String.valueOf(listingId));
        Path destination = listingDirectory.resolve(fileName).normalize();

        try {
            Files.createDirectories(listingDirectory);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo guardar la imagen");
        }

        String url = "/uploads/listings/" + listingId + "/" + fileName;
        ListingImage image = new ListingImage(
                listing,
                fileName,
                url,
                file.getContentType(),
                file.getSize()
        );

        listing.markUpdated();
        ListingImage savedImage = listingImageRepository.save(image);
        return ListingImageResponse.from(savedImage);
    }

    @Transactional
    public void deleteImage(Long listingId, Long imageId, User user) {
        Listing listing = getExistingListing(listingId);
        assertOwnerOrAdmin(listing, user);

        ListingImage image = listingImageRepository.findByIdAndListingId(imageId, listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagen no encontrada"));

        listingImageRepository.delete(image);
        listing.markUpdated();

        Path imagePath = uploadRoot.resolve("listings")
                .resolve(String.valueOf(listingId))
                .resolve(image.getFileName())
                .normalize();

        try {
            Files.deleteIfExists(imagePath);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo borrar el archivo de imagen");
        }
    }

    private void ensureListingExists(Long listingId) {
        if (!listingRepository.existsById(listingId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Anuncio no encontrado");
        }
    }

    private Listing getExistingListing(Long listingId) {
        return listingRepository.findById(listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anuncio no encontrado"));
    }

    private void assertOwnerOrAdmin(Listing listing, User user) {
        boolean isOwner = listing.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes modificar las imagenes de este anuncio");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen es obligatoria");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de imagen no permitido");
        }
    }

    private void validateImageLimit(Long listingId) {
        long currentImages = listingImageRepository.countByListingId(listingId);
        if (currentImages >= maxImagesPerListing) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Se ha alcanzado el maximo de imagenes para este anuncio");
        }
    }

    private String getExtension(String originalFilename, String contentType) {
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
        }

        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }
}
