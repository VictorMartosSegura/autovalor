package com.autovalor.api.controller.rest;

import com.autovalor.api.dto.listingDTO.ListingImageResponse;
import com.autovalor.api.service.ListingImageService;
import com.autovalor.user.User;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping({"/api/cars/{listingId}/images", "/api/v1/listings/{listingId}/images"})
public class ListingImageController {

    private final ListingImageService listingImageService;

    public ListingImageController(ListingImageService listingImageService) {
        this.listingImageService = listingImageService;
    }

    @GetMapping
    public List<ListingImageResponse> getImages(@PathVariable Long listingId) {
        return listingImageService.findImages(listingId);
    }

    @PostMapping
    public ResponseEntity<ListingImageResponse> uploadImage(
            @PathVariable Long listingId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(listingImageService.uploadImage(listingId, file, user));
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long listingId,
            @PathVariable Long imageId,
            @AuthenticationPrincipal User user
    ) {
        listingImageService.deleteImage(listingId, imageId, user);
        return ResponseEntity.noContent().build();
    }
}
