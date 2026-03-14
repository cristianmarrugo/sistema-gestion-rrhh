package com.example.demo.controller;

import com.example.demo.model.Empleado;
import com.example.demo.model.HistorialCambio;
import com.example.demo.repository.HistorialCambioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class AdminController {

    @Autowired
    private HistorialCambioRepository historialCambioRepository;

    @GetMapping("/admin/historial")
    public String verHistorial (Model model, HttpSession session){
        Empleado admin = (Empleado) session.getAttribute("empleado");

        // Validación de seguridad (Solo ADMIN)
        if (admin == null || !"ADMIN".equals(admin.getRol().toString())) {
            return "redirect:/index";
        }

        // Obtenemos los logs usando el nuevo repositorio
        List<HistorialCambio> logs = historialCambioRepository.findAllByOrderByFechaDesc();
        model.addAttribute("logs", logs);

        return "historial-cambios";
    }
}
