package com.hs.lab4.userservice.dto.kafkaDto;

import com.hs.lab4.userservice.enums.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleAddedEvent {
    private String login;
    private String role;
}
