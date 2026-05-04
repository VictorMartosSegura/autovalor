package com.autovalor.api.dto.contactDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateContactMessageRequest {

    @NotBlank
    @Size(max = 120)
    private String contactName;

    @NotBlank
    @Email
    @Size(max = 160)
    private String contactEmail;

    @Size(max = 40)
    private String contactPhone;

    @NotBlank
    @Size(max = 2000)
    private String message;

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
