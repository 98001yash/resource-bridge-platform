package com.resourcebridge.auth_service.dtos;


import com.resourcebridge.auth_service.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {

    private Long id;

    private String name;
    private String email;

    private Role role;

    private boolean verified;
}
