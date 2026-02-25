package com.example.demo.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/")
    public String index(HttpSession session) {

        if (session.getAttribute("empleado") == null) {
            return "redirect:/pin";
        }

        return "index";
    }
}

