package com.yash.delivery.service;

import com.yash.delivery.dto.LoginRequest;
import com.yash.delivery.dto.LoginResponse;
import com.yash.delivery.dto.RegisterRequest;
import com.yash.delivery.exception.ApiException;
import com.yash.delivery.model.User;
import com.yash.delivery.model.UserRole;
import com.yash.delivery.repository.UserRepository;
import com.yash.delivery.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public User registerUser(RegisterRequest request){

        // check duplicate email
        if(userRepository.existsByEmail(request.getEmail())){
            throw new ApiException("Email already registered");
        }

        // hash email
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // set default role if null
        UserRole role = request.getRole() != null? request.getRole() : UserRole.CUSTOMER;

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(hashedPassword)
                .phone(request.getPhone())
                .role(request.getRole())
                .createdAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    public LoginResponse loginUser(LoginRequest request){

        // find user by email, if not found throw exception
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException("Invalid email or password"));

        // verify password
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if(!passwordMatches){
            throw new ApiException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();

    }
}



