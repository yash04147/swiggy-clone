package com.yash.delivery.model;

import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;


@Document(collection = "Users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    private String id;

    private String name;

    private String email;

    private String password;   // Will store hashed password

    private String phone;

    private UserRole role;

    private LocalDateTime createdAt;

}
