package com.autovalor.api.controller.rest;

import com.autovalor.api.dto.listingDTO.ListingResponse;
import com.autovalor.api.service.ListingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/listings")
public class ListingController {

    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @GetMapping
    public List<ListingResponse> getAll() {
        return listingService.findAll();
    }
}
