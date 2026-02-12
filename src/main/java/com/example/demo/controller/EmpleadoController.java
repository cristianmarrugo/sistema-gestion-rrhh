package com.example.demo.controller;


import com.example.demo.model.Empleado;
import com.example.demo.service.EmpleadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empleados")
@RequiredArgsConstructor
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    // Listar todos los empleados
    @GetMapping
    public ResponseEntity<List<Empleado>> listar() {
        return ResponseEntity.ok(empleadoService.listarTodos());
    }

    // Obtener empleado por ID
    @GetMapping("/{id}")
    public ResponseEntity<Empleado> obtenerPorId(@PathVariable Long id) {
        return empleadoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Crear nuevo empleado
    @PostMapping
    public ResponseEntity<Empleado> crear(@RequestBody Empleado empleado) {
        Empleado nuevo = empleadoService.crear(empleado);
        return ResponseEntity.ok(nuevo);
    }

    // Actualizar empleado existente
    @PutMapping("/{id}")
    public ResponseEntity<Empleado> actualizar(
            @PathVariable Long id,
            @RequestBody Empleado empleado) {

        return empleadoService.actualizar(id, empleado)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Desactivar empleado (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        if (empleadoService.desactivar(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Listar solo empleados activos
    @GetMapping("/activos")
    public ResponseEntity<List<Empleado>> listarActivos() {
        return ResponseEntity.ok(empleadoService.listarActivos());
    }

    // Buscar empleados por nombre o apellido
    @GetMapping("/buscar")
    public ResponseEntity<List<Empleado>> buscar(@RequestParam String q) {
        return ResponseEntity.ok(empleadoService.buscar(q));
    }
}
