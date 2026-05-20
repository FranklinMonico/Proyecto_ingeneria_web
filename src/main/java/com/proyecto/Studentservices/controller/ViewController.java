package com.proyecto.Studentservices.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/dashboard")
    public String dashboard() { return "dashboard"; }

    @GetMapping("/forum/{courseId}")
    public String forum(@PathVariable String courseId) { return "forum"; }
    @GetMapping("/forgot-password")
    public String showForgotPassword() { return "forgot-password"; }

    @GetMapping("/reset-password")
    public String showResetPassword() { return "reset-password"; }

    @GetMapping("/profile")
    public String profile() { return "profile"; }
}
