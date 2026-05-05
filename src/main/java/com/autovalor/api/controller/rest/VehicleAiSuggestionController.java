package com.autovalor.api.controller.rest;

import com.autovalor.api.dto.aiDTO.VehicleAiSuggestionResponse;
import com.autovalor.api.service.VehicleAiSuggestionService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ai")
public class VehicleAiSuggestionController {

    private final VehicleAiSuggestionService vehicleAiSuggestionService;

    public VehicleAiSuggestionController(VehicleAiSuggestionService vehicleAiSuggestionService) {
        this.vehicleAiSuggestionService = vehicleAiSuggestionService;
    }

    @PostMapping(value = "/vehicle-suggestions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public VehicleAiSuggestionResponse suggestVehicleData(
            @RequestParam(value = "prompt", required = false) String prompt,
            @RequestParam(value = "images", required = false) List<MultipartFile> images
    ) {
        return vehicleAiSuggestionService.suggest(prompt, images);
    }
}
