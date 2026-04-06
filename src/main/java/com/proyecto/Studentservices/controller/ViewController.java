package com.proyecto.Studentservices.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/view")
@Controller public class ViewController {

    @GetMapping("/register") public String showRegister() {
        return "register"; }
    @GetMapping("/login") public String showLogin() {
        return "login"; }
    @GetMapping("/")
    public String index() {
        return "index";
    }
}
