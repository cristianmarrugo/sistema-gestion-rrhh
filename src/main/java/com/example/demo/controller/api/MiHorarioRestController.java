package com.example.demo.controller.api;

import com.example.demo.model.Empleado;
import com.example.demo.service.EmpleadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/mi-horario")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MiHorarioRestController {

    private final EmpleadoService empleadoService;

    // Flutter llamará a: GET http://tu-ip:8080/api/mi-horario/1  (Donde 1 es el ID del empleado)
    @GetMapping("/{id}")
    public Map<String, Object> obtenerMiHorario(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        Empleado empleado = empleadoService.obtenerPorId(id).orElse(null);

        if (empleado == null) {
            response.put("success", false);
            response.put("message", "Empleado no encontrado");
            return response;
        }

        response.put("success", true);
        response.put("horario", empleado.getHorario()); // Envía las especificaciones del turno asignado
        return response;
    }
}
