package com.autovalor.user.dto;

import jakarta.validation.constraints.Size;

public class UserAddressRequest {

    @Size(max = 80)
    private String country;

    @Size(max = 120)
    private String city;

    @Size(max = 255)
    private String address;

    private Double latitude;

    private Double longitude;

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
