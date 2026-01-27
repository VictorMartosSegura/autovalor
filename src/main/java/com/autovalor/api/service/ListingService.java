package com.autovalor.api.service;

import com.autovalor.api.dto.listingDTO.ListingResponse;
import com.autovalor.api.mapper.ListingMapper;
import com.autovalor.api.repository.ListingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListingService {

    private final ListingRepository listingRepository;

    public ListingService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    @Transactional(readOnly = true)
    public List<ListingResponse> findAll() {

        return listingRepository.findAll()
                .stream()
                .map(ListingMapper::toResponse)
                .toList();
    }
}
