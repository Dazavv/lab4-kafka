package com.hs.lab4.groupeventservice.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateUserGroupEventRequest(
    @NotBlank
    String name,
    String description,
    @NotNull
    List<Long> participantIds
) {}