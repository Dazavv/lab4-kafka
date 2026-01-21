package com.hs.lab4.notificationservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventNotificationMessage {
    private Long eventId;
    private String eventName;
    private String eventDescription;
    private Long userId;
    private String message;
}