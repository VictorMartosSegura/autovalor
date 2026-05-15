package com.autovalor.api.dto.listingDTO;

import com.autovalor.api.model.ListingStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class ListingResponse {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String brand;
    private String model;
    private Integer year;
    private Integer km;
    private String fuelType;
    private String transmission;
    private String location;
    private String province;
    private String sellerType;
    private String bodyType;
    private Integer doors;
    private Integer powerCv;
    private String engineSize;
    private String environmentalLabel;
    private Boolean warranty;
    private String color;
    private Integer registrationMonth;
    private Integer registrationYear;
    private Integer previousOwners;
    private Boolean financeable;
    private Boolean maintenanceBook;
    private ListingStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Long userId;
    private String userName;
    private String sellerAddressCountry;
    private String sellerAddressCity;
    private String sellerAddressLine;
    private Double sellerAddressLatitude;
    private Double sellerAddressLongitude;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public ListingStatus getStatus() { return status; }
    public void setStatus(ListingStatus status) { this.status = status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getSellerAddressCountry() { return sellerAddressCountry; }
    public void setSellerAddressCountry(String sellerAddressCountry) { this.sellerAddressCountry = sellerAddressCountry; }
    public String getSellerAddressCity() { return sellerAddressCity; }
    public void setSellerAddressCity(String sellerAddressCity) { this.sellerAddressCity = sellerAddressCity; }
    public String getSellerAddressLine() { return sellerAddressLine; }
    public void setSellerAddressLine(String sellerAddressLine) { this.sellerAddressLine = sellerAddressLine; }
    public Double getSellerAddressLatitude() { return sellerAddressLatitude; }
    public void setSellerAddressLatitude(Double sellerAddressLatitude) { this.sellerAddressLatitude = sellerAddressLatitude; }
    public Double getSellerAddressLongitude() { return sellerAddressLongitude; }
    public void setSellerAddressLongitude(Double sellerAddressLongitude) { this.sellerAddressLongitude = sellerAddressLongitude; }
}
