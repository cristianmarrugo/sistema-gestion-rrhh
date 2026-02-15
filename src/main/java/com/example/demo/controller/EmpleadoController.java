package com.example.demo.controller;

import com.example.demo.model.Empleado;
import com.example.demo.model.Rol;
import com.example.demo.service.EmpleadoService;
import jakarta.servlet.http.HttpSession;
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
    public ResponseEntity<?> crear(
            @RequestBody Empleado empleado,
            HttpSession session) {

        Empleado usuarioLogueado = (Empleado) session.getAttribute("empleado");

        if (usuarioLogueado == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        // VALIDAR PERMISOS
        // Solo ADMIN puede crear empleados con rol ADMIN
        if (empleado.getRol() == Rol.ADMIN && usuarioLogueado.getRol() != Rol.ADMIN) {
            return ResponseEntity.status(403).body("Solo ADMIN puede crear usuarios ADMIN");
        }

        Empleado nuevo = empleadoService.crear(empleado);
        return ResponseEntity.ok(nuevo);
    }

    // Actualizar empleado existente
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @RequestBody Empleado empleadoActualizado,
            HttpSession session) {

        Empleado usuarioLogueado = (Empleado) session.getAttribute("empleado");

        if (usuarioLogueado == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        // Obtener el empleado actual de la BD
        Empleado empleadoExistente = empleadoService.obtenerPorId(id)
                .orElse(null);

        if (empleadoExistente == null) {
            return ResponseEntity.notFound().build();
        }

        if (usuarioLogueado.getId().equals(id)) {
            if (empleadoActualizado.getRol() != empleadoExistente.getRol()) {
                return ResponseEntity.status(403)
                        .body("No puedes cambiar tu propio rol por seguridad. Solicita a otro ADMIN que lo haga.");
            }
        }

        // ====== VALIDACIÓN DE PERMISOS ======

        // Si el usuario es RRHH (NO es ADMIN)
        if (usuarioLogueado.getRol() == Rol.RRHH) {

            // RRHH NO puede cambiar:
            // 1. ROL
            if (empleadoActualizado.getRol() != empleadoExistente.getRol()) {
                return ResponseEntity.status(403)
                        .body("RRHH no puede cambiar roles de usuario. Solo ADMIN puede hacerlo.");
            }

            // 2. PIN
            if (empleadoActualizado.getPin() != null &&
                    !empleadoActualizado.getPin().equals(empleadoExistente.getPin())) {
                return ResponseEntity.status(403)
                        .body("RRHH no puede cambiar PINs. Solo ADMIN puede hacerlo.");
            }

            // 3. ESTADO (activo/inactivo)
            if (empleadoActualizado.isActivo() != empleadoExistente.isActivo()) {
                return ResponseEntity.status(403)
                        .body("RRHH no puede activar/desactivar usuarios. Solo ADMIN puede hacerlo.");
            }

            if (empleadoActualizado.getDocumento() != null &&
                    !empleadoActualizado.getDocumento().equals(empleadoExistente.getDocumento())){
                return ResponseEntity.status(403)
                        .body("RRHH no puede cambiar el documento");


            }

            // FORZAR que mantengan los valores originales
            empleadoActualizado.setRol(empleadoExistente.getRol());
            empleadoActualizado.setPin(empleadoExistente.getPin());
            empleadoActualizado.setActivo(empleadoExistente.isActivo());
            empleadoActualizado.setDocumento(empleadoExistente.getDocumento());
        }

        // Si el usuario es ADMIN, puede cambiar todo
        // (no hay restricciones adicionales)

        return empleadoService.actualizar(id, empleadoActualizado)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Desactivar empleado (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> desactivar(
            @PathVariable Long id,
            HttpSession session) {

        Empleado usuarioLogueado = (Empleado) session.getAttribute("empleado");

        if (usuarioLogueado == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        // Solo ADMIN puede desactivar empleados
        if (usuarioLogueado.getRol() != Rol.ADMIN) {
            return ResponseEntity.status(403)
                    .body("Solo ADMIN puede desactivar empleados");
        }

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
