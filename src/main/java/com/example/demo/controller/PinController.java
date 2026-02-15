package com.example.demo.controller;

import com.example.demo.model.Empleado;
import com.example.demo.repository.EmpleadoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class PinController {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @GetMapping("/pin")
    public String pin() {
        return "pin";
    }

    @PostMapping("/pin")
    public String procesarPin(@RequestParam String pin, HttpSession session) {

        Optional<Empleado> empleadoOpt = empleadoRepository.findByPin(pin);

        if (empleadoOpt.isEmpty()) {
            return "redirect:/pin?error";
        }

        Empleado empleado = empleadoOpt.get();

        if (!empleado.isActivo()) {
            return "redirect:/pin?error=inactive";
        }

        session.setAttribute("empleado", empleado);
        session.setAttribute("rol", empleado.getRol());

        return "redirect:/index";
    }
}



