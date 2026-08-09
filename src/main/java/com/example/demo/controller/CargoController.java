package com.example.demo.controller;

import com.example.demo.model.Cargo;
import com.example.demo.service.CargoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cargos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CargoController {

    private final CargoService cargoService;

    // Listar todos los cargos
    @GetMapping
    public ResponseEntity<List<Cargo>> listar() {
        return ResponseEntity.ok(cargoService.listarTodos());
    }

    // Obtener cargo por ID
    @GetMapping("/{id}")
    public ResponseEntity<Cargo> obtenerPorId(@PathVariable Long id) {
        return cargoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Crear nuevo cargo
    @PostMapping
    public ResponseEntity<Cargo> crear(@RequestBody Cargo cargo) {
        Cargo nuevo = cargoService.crear(cargo);
        return ResponseEntity.ok(nuevo);
    }

    // Actualizar cargo existente
    @PutMapping("/{id}")
    public ResponseEntity<Cargo> actualizar(
            @PathVariable Long id,
            @RequestBody Cargo cargo) {

        return cargoService.actualizar(id, cargo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminar cargo
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (cargoService.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
