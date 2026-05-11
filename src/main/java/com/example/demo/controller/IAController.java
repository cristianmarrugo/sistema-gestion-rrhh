package com.example.demo.controller;

import com.example.demo.dto.AnalisisIA;
import com.example.demo.service.RedNeuronalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/ia-prediccion")
public class IAController {

    @Autowired
    private RedNeuronalService rnService;

    // Carga la página inicial
    @GetMapping
    public String verModuloIA(Model model) {
        if (!model.containsAttribute("analisis")) {
            model.addAttribute("analisis", new AnalisisIA());
        }
        return "admin/modulo-ia";
    }

    // Maneja el caso de "Refrescar" la página después de una predicción
    // Evita el error "GET not supported"
    @GetMapping("/analizar")
    public String redireccionarFormulario() {
        return "redirect:/ia-prediccion";
    }

    // Procesa la predicción (POST)
    @PostMapping("/analizar")
    public String procesarIA(@ModelAttribute AnalisisIA analisis, Model model) {
        // 1. Llamamos al servicio UNA SOLA VEZ y guardamos la respuesta (ej: "PUNTUAL,95.5")
        String respuestaRaw = rnService.ejecutarPrediccion(
                analisis.getDiaSemana(),
                analisis.getMinutosRetraso(),
                analisis.getReferenciaUbicacion()
        );

        // 2. Dividimos la respuesta por la coma
        String[] partes = respuestaRaw.split(",");

        // 3. Verificamos que Python devolvió ambos datos para evitar errores de índice
        if (partes.length >= 2) {
            analisis.setResultadoPrediccion(partes[0]); // "PUNTUAL" o "TARDE"
            analisis.setProbabilidad(partes[1]);        // "95.5"
        } else {
            analisis.setResultadoPrediccion(partes[0]);
            analisis.setProbabilidad("0");
        }

        model.addAttribute("analisis", analisis);

        // 4. Retornamos la vista (sin redirect para mantener los datos en pantalla)
        return "admin/modulo-ia";
    }
}