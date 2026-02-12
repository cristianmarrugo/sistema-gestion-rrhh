package com.example.demo.controller;

import com.example.demo.model.Empleado;
import com.example.demo.model.Permiso;
import com.example.demo.service.PermisoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permisos")
@RequiredArgsConstructor
public class PermisoController {

    private final PermisoService permisoService;

    // Listar todos los permisos (ADMIN/RRHH)
    @GetMapping
    public ResponseEntity<List<Permiso>> listar() {
        return ResponseEntity.ok(permisoService.listarTodos());
    }

    // Listar permisos pendientes de aprobación (ADMIN/RRHH)
    @GetMapping("/pendientes")
    public ResponseEntity<List<Permiso>> listarPendientes() {
        return ResponseEntity.ok(permisoService.listarPendientes());
    }

    // Listar mis permisos (EMPLEADO)
    @GetMapping("/mis-permisos")
    public ResponseEntity<List<Permiso>> listarMisPermisos(HttpSession session) {
        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(permisoService.listarPorEmpleado(empleado.getId()));
    }

    // Solicitar permiso
    @PostMapping("/solicitar")
    public ResponseEntity<Permiso> solicitar(
            @RequestBody Permiso permiso,
            HttpSession session) {

        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return ResponseEntity.status(401).build();
        }

        Permiso nuevo = permisoService.solicitar(permiso, empleado);
        return ResponseEntity.ok(nuevo);
    }

    // Aprobar permiso (ADMIN/RRHH)
    @PutMapping("/{id}/aprobar")
    public ResponseEntity<Permiso> aprobar(@PathVariable Long id) {
        return permisoService.aprobar(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Rechazar permiso (ADMIN/RRHH)
    @PutMapping("/{id}/rechazar")
    public ResponseEntity<Permiso> rechazar(@PathVariable Long id) {
        return permisoService.rechazar(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminar permiso
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (permisoService.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
