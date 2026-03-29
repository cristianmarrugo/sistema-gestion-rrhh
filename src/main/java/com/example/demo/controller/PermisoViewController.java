package com.example.demo.controller;

import com.example.demo.model.Permiso;
import com.example.demo.service.PermisoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller // Sin "Rest", para que devuelva vistas HTML
@RequestMapping("/permisos")
@RequiredArgsConstructor
public class PermisoViewController {

    private final PermisoService permisoService;

    @GetMapping("/{id}/historial")
    public String verHistorial(@PathVariable Long id, Model model) {
        List<Permiso> historial = permisoService.obtenerHistorial(id);
        model.addAttribute("historial", historial);
        return "permisos/historial";
    }
}
