package com.example.demo.controller;


import com.example.demo.model.Vacacion;
import com.example.demo.service.VacacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/vacaciones")
@RequiredArgsConstructor
public class VacacionViewController {

    private final VacacionService vacacionService;

    @GetMapping("/{id}/historial")
    public String verHistorial(@PathVariable Long id, Model model) {
        List<Vacacion> historial = vacacionService.obtenerHistorial(id);

        // Reutilizamos la lógica del historial
        model.addAttribute("historial", historial);
        model.addAttribute("titulo", "Historial de Vacaciones");

        return "vacaciones/historial";
    }
}
