package com.hs.lab4.userservice.dto.requests;

import lombok.Data;

@Data
public class RefreshJwtRequest {
    public String refreshToken;
}
