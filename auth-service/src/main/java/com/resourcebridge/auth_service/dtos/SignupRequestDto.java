package com.resourcebridge.auth_service.dtos;


import com.resourcebridge.auth_service.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRequestDto {

    @NotBlank(message = "Name is required")
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 6)
    private String password;

    @NotNull(message = "Role is required")
    private Role role;

    @NotBlank(message = "Address is required")
    private String address;

    private String organizationName;
}
