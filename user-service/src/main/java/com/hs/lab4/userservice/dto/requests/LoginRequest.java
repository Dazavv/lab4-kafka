package com.hs.lab4.userservice.dto.requests;

import lombok.Data;

@Data
public class LoginRequest {
    private String login;
    private String password;
}