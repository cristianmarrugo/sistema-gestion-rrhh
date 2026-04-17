package com.example.demo.controller;


import com.example.demo.service.WekaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("/prediccion")
public class PredictionController {

    @Autowired
    private WekaService wekaService;

    @GetMapping
    public String mostrarPagina(Model model) {
        return "prediccion"; // Esto busca prediccion.html en templates
    }

    @PostMapping("/calcular")
    public String calcular(@RequestParam int dia, @RequestParam double retraso, Model model) {
        try {
            // 1. Obtener predicción y confianza para el caso actual
            Map<String, Object> prediccion = wekaService.predecirConProbabilidad(dia, retraso, "Villa grande");

            model.addAttribute("resultado", prediccion.get("resultado"));
            model.addAttribute("confianza", prediccion.get("confianza")); // El % que cambia
            model.addAttribute("promedio", retraso);

            // 2. Mantener tu lógica de análisis semanal (Opcional)
            StringBuilder diasRiesgo = new StringBuilder("Días con riesgo: ");
            for (int i = 1; i <= 7; i++) {
                Map<String, Object> p = wekaService.predecirConProbabilidad(i, retraso, "Villa grande");
                if ("TARDE".equals(p.get("resultado"))) {
                    diasRiesgo.append(traducirDia(i)).append(" ");
                }
            }
            model.addAttribute("analisisDias", "Análisis semanal: " + diasRiesgo.toString());

        } catch (Exception e) {
            model.addAttribute("error", "Error en el motor de IA: " + e.getMessage());
        }
        return "prediccion";
    }

    private String traducirDia(int d) {
        return switch (d) {
            case 2 -> "Lunes"; case 3 -> "Martes"; case 4 -> "Miércoles";
            case 5 -> "Jueves"; case 6 -> "Viernes"; case 7 -> "Sábado";
            default -> "Domingo";
        };
    }
}
