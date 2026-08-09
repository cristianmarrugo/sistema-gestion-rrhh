package com.example.demo.controller.api;

import com.example.demo.model.Turno;
import com.example.demo.repository.TurnoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TurnoRestController {

    private final TurnoRepository turnoRepository;

    // 1. LISTAR TURNOS
    @GetMapping
    public List<Turno> listarTurnos() {
        return turnoRepository.findAll();
    }

    // 2. OBTENER DETALLE DE UN TURNO
    @GetMapping("/{id}")
    public Turno obtenerTurno(@PathVariable Long id) {
        return turnoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Turno no encontrado"));
    }

    // 3. CREAR O EDITAR TURNO (Guarda la entidad enviada desde Flutter como JSON)
    @PostMapping("/guardar")
    public Map<String, Object> guardarTurno(@RequestBody Turno turno) {
        Map<String, Object> response = new HashMap<>();
        try {
            Turno guardado = turnoRepository.save(turno);
            response.put("success", true);
            response.put("turno", guardado);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // 4. ELIMINAR TURNO
    @DeleteMapping("/eliminar/{id}")
    public Map<String, Object> eliminarTurno(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            turnoRepository.deleteById(id);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
}
