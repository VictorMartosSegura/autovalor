package com.autovalor.api.controller.rest;

import com.autovalor.api.dto.adminDTO.AdminStatsResponse;
import com.autovalor.api.dto.adminDTO.AdminUserResponse;
import com.autovalor.api.dto.adminDTO.UpdateUserRoleRequest;
import com.autovalor.api.dto.contactDTO.ContactMessageResponse;
import com.autovalor.api.dto.listingDTO.ListingResponse;
import com.autovalor.api.dto.listingDTO.UpdateListingStatusRequest;
import com.autovalor.api.service.AdminService;
import com.autovalor.user.User;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public AdminStatsResponse getStats() {
        return adminService.getStats();
    }

    @GetMapping("/users")
    public List<AdminUserResponse> getUsers() {
        return adminService.findUsers();
    }

    @PatchMapping("/users/{userId}/role")
    public AdminUserResponse updateUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRoleRequest request,
            @AuthenticationPrincipal User admin
    ) {
        return adminService.updateUserRole(userId, request.role(), admin);
    }

    @GetMapping("/listings")
    public List<ListingResponse> getListings() {
        return adminService.findListings();
    }

    @PatchMapping("/listings/{listingId}/status")
    public ListingResponse updateListingStatus(
            @PathVariable Long listingId,
            @Valid @RequestBody UpdateListingStatusRequest request
    ) {
        return adminService.updateListingStatus(listingId, request.status());
    }

    @DeleteMapping("/listings/{listingId}")
    public ResponseEntity<Void> deleteListing(@PathVariable Long listingId) {
        adminService.deleteListing(listingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/contact-messages")
    public List<ContactMessageResponse> getContactMessages() {
        return adminService.findContactMessages();
    }
}
