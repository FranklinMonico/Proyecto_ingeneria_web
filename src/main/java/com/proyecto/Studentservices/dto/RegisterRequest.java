package com.proyecto.Studentservices.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class RegisterRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @Email(message = "Email inválido")
    @NotBlank(message = "El email es obligatorio")
    private String email;

    @NotBlank(message = "El apellido es obligatorio")
    private String lastName;

    @Size(min = 6, message = "La contraseña debe tener mínimo 6 caracteres")
    private String password;

    private String profilePicture;
}
