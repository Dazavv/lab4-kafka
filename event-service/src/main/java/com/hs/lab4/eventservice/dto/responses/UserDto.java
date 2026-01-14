package com.hs.lab4.eventservice.dto.responses;

import java.util.List;

public record UserDto(
        Long id,
        String username,
        String name,
        String surname,
        List<String> eventNames
) {}
