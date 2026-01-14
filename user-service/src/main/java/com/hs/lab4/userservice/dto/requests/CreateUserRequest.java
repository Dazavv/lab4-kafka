package com.hs.lab4.userservice.dto.requests;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank String username,
        @NotBlank String password,
        String name,
        String surname
) {}
