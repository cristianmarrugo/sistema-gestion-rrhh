package com.example.demo.controller;

import com.example.demo.model.Empleado;
import com.example.demo.model.Vacacion;
import com.example.demo.service.VacacionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vacaciones")
@RequiredArgsConstructor
public class VacacionController {

    private final VacacionService vacacionService;

    // Listar todas las vacaciones (ADMIN/RRHH)
    @GetMapping
    public ResponseEntity<List<Vacacion>> listar() {
        return ResponseEntity.ok(vacacionService.listarTodas());
    }

    // Listar vacaciones pendientes de aprobación (ADMIN/RRHH)
    @GetMapping("/pendientes")
    public ResponseEntity<List<Vacacion>> listarPendientes() {
        return ResponseEntity.ok(vacacionService.listarPendientes());
    }

    // Listar mis vacaciones (EMPLEADO)
    @GetMapping("/mis-vacaciones")
    public ResponseEntity<List<Vacacion>> listarMisVacaciones(HttpSession session) {
        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(vacacionService.listarPorEmpleado(empleado.getId()));
    }

    // Consultar días disponibles
    @GetMapping("/dias-disponibles")
    public ResponseEntity<Integer> consultarDiasDisponibles(HttpSession session) {
        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return ResponseEntity.status(401).build();
        }

        int dias = vacacionService.diasDisponibles(empleado);
        return ResponseEntity.ok(dias);
    }

    // Solicitar vacaciones
    @PostMapping("/solicitar")
    public ResponseEntity<Vacacion> solicitar(
            @RequestBody Vacacion vacacion,
            HttpSession session) {

        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            Vacacion nueva = vacacionService.solicitar(vacacion, empleado);
            return ResponseEntity.ok(nueva);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Aprobar vacaciones (ADMIN/RRHH)
    @PutMapping("/{id}/aprobar")
    public ResponseEntity<Vacacion> aprobar(@PathVariable Long id) {
        return vacacionService.aprobar(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Rechazar vacaciones (ADMIN/RRHH)
    @PutMapping("/{id}/rechazar")
    public ResponseEntity<Vacacion> rechazar(@PathVariable Long id) {
        return vacacionService.rechazar(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminar solicitud de vacaciones
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (vacacionService.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
