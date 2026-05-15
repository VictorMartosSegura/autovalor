package com.autovalor.api.service;

import com.autovalor.api.dto.listingDTO.ListingImageResponse;
import com.autovalor.api.model.Listing;
import com.autovalor.api.model.ListingImage;
import com.autovalor.api.repository.ListingImageRepository;
import com.autovalor.api.repository.ListingRepository;
import com.autovalor.user.Role;
import com.autovalor.user.User;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
import java.util.List;
import java.util.Map;
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
    private final Cloudinary cloudinary;
    private final int maxImagesPerListing;
    private final String cloudinaryFolder;

    public ListingImageService(
            ListingRepository listingRepository,
            ListingImageRepository listingImageRepository,
            Cloudinary cloudinary,
            @Value("${app.upload.max-images-per-listing:6}") int maxImagesPerListing,
            @Value("${app.cloudinary.folder:autovalor/listings}") String cloudinaryFolder
    ) {
        this.listingRepository = listingRepository;
        this.listingImageRepository = listingImageRepository;
        this.cloudinary = cloudinary;
        this.maxImagesPerListing = maxImagesPerListing;
        this.cloudinaryFolder = cloudinaryFolder;
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
        String publicIdWithoutFolder = removeExtension(fileName);
        String fallbackPublicId = cloudinaryFolder + "/" + listingId + "/" + publicIdWithoutFolder;

        Map<?, ?> uploadResult;
        try {
            uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", cloudinaryFolder + "/" + listingId,
                    "public_id", publicIdWithoutFolder,
                    "resource_type", "image",
                    "overwrite", true
            ));
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo subir la imagen a Cloudinary");
        }

        String url = String.valueOf(uploadResult.get("secure_url"));
        Object publicIdValue = uploadResult.get("public_id");
        String savedPublicId = publicIdValue == null ? fallbackPublicId : String.valueOf(publicIdValue);

        ListingImage image = new ListingImage(
                listing,
                savedPublicId,
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

        try {
            cloudinary.uploader().destroy(image.getFileName(), ObjectUtils.asMap("resource_type", "image"));
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo borrar la imagen de Cloudinary");
        }

        listingImageRepository.delete(image);
        listing.markUpdated();
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

    private String removeExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }
}
