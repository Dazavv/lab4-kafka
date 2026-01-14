package com.hs.lab4.userservice.dto.responses;

import com.hs.lab4.userservice.enums.Role;
import java.util.Set;

public record UserDto(
        Long id,
        String login,
        String name,
        String surname,
        String email,
        Set<Role> roles
) {}
