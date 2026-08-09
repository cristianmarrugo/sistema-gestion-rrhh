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
@CrossOrigin(origins = "*")
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
    public ResponseEntity<?> solicitar(
            @RequestBody Permiso permiso,
            HttpSession session) {
        try {
            Empleado empleado = (Empleado) session.getAttribute("empleado");

            if (empleado == null) {
                return ResponseEntity.status(401).build();
            }

            Permiso nuevo = permisoService.solicitar(permiso, empleado);

            return ResponseEntity.ok(nuevo);

        } catch (Exception e) {

            return ResponseEntity.badRequest().body(e.getMessage());
        }


    }

    // Aprobar permiso (ADMIN/RRHH)
    @PutMapping("/{id}/aprobar")
    public ResponseEntity<?> aprobar(@PathVariable Long id, HttpSession session) {
        Empleado adminLogueado = (Empleado) session.getAttribute("empleado");

        if (adminLogueado == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        try {
            // LLAMAMOS AL MÉTODO QUE TIENE LA VALIDACIÓN DE SEGURIDAD
            permisoService.autorizarPermiso(id, adminLogueado.getNombre(), adminLogueado);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            // Aquí es donde capturamos el "No puedes aprobar tu propia solicitud"
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/rechazar")
    public ResponseEntity<?> rechazar(@PathVariable Long id, HttpSession session) {
        Empleado admin = (Empleado) session.getAttribute("empleado");
        if (admin == null) return ResponseEntity.status(401).body("No autenticado");

        try {
            permisoService.rechazarPermiso(id, admin);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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
