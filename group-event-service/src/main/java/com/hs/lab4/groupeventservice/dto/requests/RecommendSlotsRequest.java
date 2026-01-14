package com.hs.lab4.groupeventservice.dto.requests;

import java.time.Duration;
import java.time.LocalDate;

public record RecommendSlotsRequest(
        LocalDate periodStart,
        LocalDate periodEnd,
        Duration duration,
        Long groupEventId
) {
}