package com.proyecto.Studentservices.controller;


import com.proyecto.Studentservices.util.SecurityUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping
    public String test() {
        String email = SecurityUtils.getCurrentUserEmail();
        return "Usuario autenticado: " + email;
    }
}