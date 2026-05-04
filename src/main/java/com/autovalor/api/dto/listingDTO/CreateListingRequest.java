package com.autovalor.api.dto.listingDTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotBlank
    @Size(max = 80)
    private String brand;

    @NotBlank
    @Size(max = 80)
    private String model;

    @NotNull
    @Min(1900)
    @Max(2100)
    private Integer year;

    @NotNull
    @Min(0)
    private Integer km;

    @Size(max = 40)
    private String fuelType;

    @Size(max = 40)
    private String transmission;

    @Size(max = 120)
    private String location;

    @Size(max = 80)
    private String province;

    @Size(max = 40)
    private String sellerType;

    @Size(max = 60)
    private String bodyType;

    @Min(2)
    @Max(6)
    private Integer doors;

    @Min(1)
    @Max(2000)
    private Integer powerCv;

    @Size(max = 40)
    private String engineSize;

    @Size(max = 40)
    private String environmentalLabel;

    private Boolean warranty;

    @Size(max = 60)
    private String color;

    @Min(1)
    @Max(12)
    private Integer registrationMonth;

    @Min(1900)
    @Max(2100)
    private Integer registrationYear;

    @Min(0)
    @Max(20)
    private Integer previousOwners;

    private Boolean financeable;

    private Boolean maintenanceBook;

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

    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }

    public String getTransmission() { return transmission; }
    public void setTransmission(String transmission) { this.transmission = transmission; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getSellerType() { return sellerType; }
    public void setSellerType(String sellerType) { this.sellerType = sellerType; }

    public String getBodyType() { return bodyType; }
    public void setBodyType(String bodyType) { this.bodyType = bodyType; }

    public Integer getDoors() { return doors; }
    public void setDoors(Integer doors) { this.doors = doors; }

    public Integer getPowerCv() { return powerCv; }
    public void setPowerCv(Integer powerCv) { this.powerCv = powerCv; }

    public String getEngineSize() { return engineSize; }
    public void setEngineSize(String engineSize) { this.engineSize = engineSize; }

    public String getEnvironmentalLabel() { return environmentalLabel; }
    public void setEnvironmentalLabel(String environmentalLabel) { this.environmentalLabel = environmentalLabel; }

    public Boolean getWarranty() { return warranty; }
    public void setWarranty(Boolean warranty) { this.warranty = warranty; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Integer getRegistrationMonth() { return registrationMonth; }
    public void setRegistrationMonth(Integer registrationMonth) { this.registrationMonth = registrationMonth; }

    public Integer getRegistrationYear() { return registrationYear; }
    public void setRegistrationYear(Integer registrationYear) { this.registrationYear = registrationYear; }

    public Integer getPreviousOwners() { return previousOwners; }
    public void setPreviousOwners(Integer previousOwners) { this.previousOwners = previousOwners; }

    public Boolean getFinanceable() { return financeable; }
    public void setFinanceable(Boolean financeable) { this.financeable = financeable; }

    public Boolean getMaintenanceBook() { return maintenanceBook; }
    public void setMaintenanceBook(Boolean maintenanceBook) { this.maintenanceBook = maintenanceBook; }
}
