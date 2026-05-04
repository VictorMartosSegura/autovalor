package com.autovalor.api.controller.rest;

import com.autovalor.api.dto.listingDTO.CreateListingRequest;
import com.autovalor.api.dto.listingDTO.ListingPageResponse;
import com.autovalor.api.dto.listingDTO.ListingResponse;
import com.autovalor.api.dto.listingDTO.UpdateListingStatusRequest;
import com.autovalor.api.model.ListingStatus;
import com.autovalor.api.service.ListingService;
import com.autovalor.user.User;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/cars", "/api/v1/listings"})
public class ListingController {

    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @GetMapping
    public ListingPageResponse searchPublic(
            @RequestParam(required = false, name = "q") String query,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minYear,
            @RequestParam(required = false) Integer maxYear,
            @RequestParam(required = false) Integer minKm,
            @RequestParam(required = false) Integer maxKm,
            @RequestParam(required = false) String fuelType,
            @RequestParam(required = false) String transmission,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String sellerType,
            @RequestParam(required = false) String bodyType,
            @RequestParam(required = false) Integer doors,
            @RequestParam(required = false) Integer minPowerCv,
            @RequestParam(required = false) Integer maxPowerCv,
            @RequestParam(required = false) String environmentalLabel,
            @RequestParam(required = false) Boolean warranty,
            @RequestParam(required = false) Boolean financeable,
            @RequestParam(required = false) ListingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "newest") String sort
    ) {
        return listingService.searchPublic(
                query,
                brand,
                model,
                minPrice,
                maxPrice,
                minYear,
                maxYear,
                minKm,
                maxKm,
                fuelType,
                transmission,
                location,
                province,
                sellerType,
                bodyType,
                doors,
                minPowerCv,
                maxPowerCv,
                environmentalLabel,
                warranty,
                financeable,
                status,
                page,
                size,
                sort
        );
    }

    @GetMapping("/all")
    public List<ListingResponse> getAllPublicLegacy() {
        return listingService.findAllPublic();
    }

    @GetMapping("/{id}")
    public ListingResponse getById(@PathVariable Long id) {
        return listingService.findById(id);
    }

    @GetMapping("/me")
    public List<ListingResponse> getMine(@AuthenticationPrincipal User user) {
        return listingService.findMine(user);
    }

    @PostMapping
    public ResponseEntity<ListingResponse> create(
            @Valid @RequestBody CreateListingRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(listingService.create(request, user));
    }

    @PutMapping("/{id}")
    public ListingResponse update(
            @PathVariable Long id,
            @Valid @RequestBody CreateListingRequest request,
            @AuthenticationPrincipal User user
    ) {
        return listingService.update(id, request, user);
    }

    @PatchMapping("/{id}/status")
    public ListingResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateListingStatusRequest request,
            @AuthenticationPrincipal User user
    ) {
        return listingService.updateStatus(id, request.status(), user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        listingService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
