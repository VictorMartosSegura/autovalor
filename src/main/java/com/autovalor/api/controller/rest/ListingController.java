package com.autovalor.api.controller.rest;

import com.autovalor.api.dto.listingDTO.CreateListingRequest;
import com.autovalor.api.dto.listingDTO.ListingResponse;
import com.autovalor.api.dto.listingDTO.UpdateListingStatusRequest;
import com.autovalor.api.service.ListingService;
import com.autovalor.user.User;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/cars", "/api/v1/listings"})
public class ListingController {

    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @GetMapping
    public List<ListingResponse> getAllPublic() {
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
