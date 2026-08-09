package com.example.demo.controller.api;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/configuracion")
@CrossOrigin(origins = "*")
public class ConfiguracionRestController {

    // Flutter llamará a: POST http://tu-ip:8080/api/configuracion/guardar
    @PostMapping("/guardar")
    public Map<String, Object> guardarConfiguracion(
            @RequestParam String nombreEmpresa,
            @RequestParam(required = false) String logo,
            @RequestParam int horaInicioLaboral,
            @RequestParam int minutosToleranciaEntrada) {

        Map<String, Object> response = new HashMap<>();
        try {
            // Aquí procesas el guardado en tu DB cuando crees la entidad/tabla Config.

            response.put("success", true);
            response.put("message", "Configuración guardada correctamente desde el móvil");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
}
