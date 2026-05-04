package com.autovalor.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangeSecretRequest {

    @NotBlank
    private String currentSecret;

    @NotBlank
    @Size(min = 8, max = 120)
    private String newSecret;

    public String getCurrentSecret() {
        return currentSecret;
    }

    public void setCurrentSecret(String currentSecret) {
        this.currentSecret = currentSecret;
    }

    public String getNewSecret() {
        return newSecret;
    }

    public void setNewSecret(String newSecret) {
        this.newSecret = newSecret;
    }
}
