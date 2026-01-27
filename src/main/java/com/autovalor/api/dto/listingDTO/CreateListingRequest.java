package com.autovalor.api.dto.listingDTO;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class CreateListingRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    @Size(max = 5000)
    private String description;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal price;

    @Size(max = 80)
    private String brand;

    @Size(max = 80)
    private String model;

    @Min(1900) @Max(2100)
    private Integer year;

    @Min(0)
    private Integer km;

    // getters/setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getKm() { return km; }
    public void setKm(Integer km) { this.km = km; }
}
