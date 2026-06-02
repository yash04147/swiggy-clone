package com.yash.delivery.dto;

import com.yash.delivery.model.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String token;

    private String userId;

    private String email;

    private UserRole role;
}
