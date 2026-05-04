package com.autovalor.api.dto.aiDTO;

import java.util.List;

public record VehicleAiSuggestionResponse(
        String title,
        String description,
        String brand,
        String model,
        Integer year,
        Integer km,
        String fuelType,
        String transmission,
        String location,
        String province,
        String sellerType,
        String bodyType,
        Integer doors,
        Integer powerCv,
        String engineSize,
        String environmentalLabel,
        Boolean warranty,
        String color,
        Integer registrationMonth,
        Integer registrationYear,
        Integer previousOwners,
        Boolean financeable,
        Boolean maintenanceBook,
        Double confidence,
        List<String> warnings
) {
}
