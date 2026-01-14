package com.hs.lab4.groupeventservice.dto.responses;

import java.time.LocalDate;
import java.time.LocalTime;

public record RecommendTimeSlotDto(
        LocalDate date,
        LocalTime start,
        LocalTime end
) {}
