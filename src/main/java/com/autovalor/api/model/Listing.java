package com.autovalor.api.model;

import com.autovalor.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "listings")
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(length = 80)
    private String brand;

    @Column(length = 80)
    private String model;

    @Column(name = "manufacture_year")
    private Integer year;

    @Column(name = "kilometers")
    private Integer km;

    @Column(name = "fuel_type", length = 40)
    private String fuelType;

    @Column(length = 40)
    private String transmission;

    @Column(length = 120)
    private String location;

    @Column(length = 80)
    private String province;

    @Column(name = "seller_type", length = 40)
    private String sellerType;

    @Column(name = "body_type", length = 60)
    private String bodyType;

    private Integer doors;

    @Column(name = "power_cv")
    private Integer powerCv;

    @Column(name = "engine_size", length = 40)
    private String engineSize;

    @Column(name = "environmental_label", length = 40)
    private String environmentalLabel;

    private Boolean warranty;

    @Column(length = 60)
    private String color;

    @Column(name = "registration_month")
    private Integer registrationMonth;

    @Column(name = "registration_year")
    private Integer registrationYear;

    @Column(name = "previous_owners")
    private Integer previousOwners;

    @Column(name = "is_financeable")
    private Boolean financeable;

    @Column(name = "has_maintenance_book")
    private Boolean maintenanceBook;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ListingStatus status = ListingStatus.AVAILABLE;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public Long getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

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
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void markUpdated() { this.updatedAt = OffsetDateTime.now(); }
}
