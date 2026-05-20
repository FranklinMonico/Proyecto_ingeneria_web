package com.proyecto.Studentservices.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    private String profilePicture;

    private boolean enabled = false;

    private String verificationToken;

    private String resetToken;
    private String lastName;

    private Date resetTokenExpiration;
    private String refreshToken;
}