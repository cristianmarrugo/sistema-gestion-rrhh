package com.example.demo.controller;


import com.example.demo.model.Horario;
import com.example.demo.service.HorarioService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HorarioController {

    private final HorarioService horarioService;

    // Listar todos los horarios
    @GetMapping
    public ResponseEntity<List<Horario>> listar() {
        return ResponseEntity.ok(horarioService.listarTodos());
    }

    // Obtener horario por ID
    @GetMapping("/{id}")
    public ResponseEntity<Horario> obtenerPorId(@PathVariable Long id) {
        return horarioService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());


    }

    // Crear nuevo horario
    @PostMapping
    public ResponseEntity<Horario> crear(@RequestBody Horario horario) {
        Horario nuevo = horarioService.crear(horario);
        return ResponseEntity.ok(nuevo);
    }

    // Actualizar horario existente
    @PutMapping("/{id}")
    public ResponseEntity<Horario> actualizar(
            @PathVariable Long id,
            @RequestBody Horario horario) {

        return horarioService.actualizar(id, horario)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminar horario
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, HttpSession session) {

        if ("RRHH".equals(session.getAttribute("rol"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (horarioService.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
