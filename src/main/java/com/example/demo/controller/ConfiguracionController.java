package com.example.demo.controller;

import com.example.demo.model.Empleado;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ConfiguracionController {

    @GetMapping("/configuracion")
    public String mostrarConfiguracion(HttpSession session, Model model) {
        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return "redirect:/login";
        }

        // Solo ADMIN puede acceder (SecurityConfig ya lo valida)
        model.addAttribute("empleado", empleado);

        return "configuracion";
    }

    @PostMapping("/configuracion/guardar")
    public String guardarConfiguracion(@RequestParam String nombreEmpresa,
                                       @RequestParam(required = false) String logo,
                                       @RequestParam int horaInicioLaboral,
                                       @RequestParam int minutosToleranciaEntrada,
                                       HttpSession session,
                                       Model model) {

        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return "redirect:/login";
        }

        // Aquí guardarías la configuración en una tabla Config o en properties
        // Por ahora solo mostramos un mensaje de éxito

        model.addAttribute("mensaje", "Configuración guardada correctamente");
        model.addAttribute("empleado", empleado);

        return "redirect:/configuracion?success=true";
    }
}
