package com.example.demo.controller;

import com.example.demo.model.Empleado;
import com.example.demo.service.EmpleadoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MiHorarioController {

    private final EmpleadoService empleadoService;

    @GetMapping("/mi-horario")
    public String verMiHorario(HttpSession session, Model model) {
        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return "redirect:/login";
        }

        // Recargar desde BD para tener horario actualizado
        empleado = empleadoService.obtenerPorId(empleado.getId()).orElse(empleado);
        model.addAttribute("empleado", empleado);
        model.addAttribute("horario", empleado.getHorario());

        return "mi-horario";
    }
}
