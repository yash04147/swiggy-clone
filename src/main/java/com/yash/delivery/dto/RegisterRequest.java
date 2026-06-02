package com.yash.delivery.dto;

import com.yash.delivery.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Invalid email")
    @NotBlank(message = "Email is required")
    private String email;

    @Size(min = 6, message = "Password must have atleast 6 characters")
    private String password;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone no. must be 10 digits")
    private String phone;

    private UserRole role;
}
