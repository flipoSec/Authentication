package com.auth.authapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotBlank
    private String currentPassword;
    @NotBlank
    @Size(min=10)
    private String newPassword;
    @NotBlank
    @Size(min=10)
    private String confirmNewPassword;
}
