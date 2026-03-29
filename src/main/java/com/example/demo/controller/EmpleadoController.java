package com.example.demo.controller;

import com.example.demo.model.Empleado;
import com.example.demo.model.Rol;
import com.example.demo.service.AuditoriaService;
import com.example.demo.service.EmpleadoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/empleados")
@RequiredArgsConstructor
public class EmpleadoController {

    private final EmpleadoService empleadoService;
    private final AuditoriaService auditoriaService;

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

        auditoriaService.registrar(
                "EMPLEADO",
                empleado.getId(),
                "CREACIÓN",
                String.format("Se registró al nuevo empleado: %s %s con cargo %s",
                        empleado.getNombre(),
                        empleado.getApellido(),
                        empleado.getCargo().getNombre()),
                usuarioLogueado
        );

        Empleado nuevo = empleadoService.crear(empleado);
        return ResponseEntity.ok(nuevo);
    }

    // Actualizar empleado existente
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @RequestBody Empleado empleadoActualizado,
            HttpSession session) {

        // 1. Obtener quién está haciendo la acción
        Empleado usuarioLogueado = (Empleado) session.getAttribute("empleado");

        if (usuarioLogueado == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        // 2. Obtener el empleado tal cual está en la BD actualmente
        Empleado empleadoExistente = empleadoService.obtenerPorId(id).orElse(null);
        if (empleadoExistente == null) {
            return ResponseEntity.notFound().build();
        }

        // ====== INICIO DE VALIDACIONES DE SEGURIDAD (Tus reglas originales) ======

        // Impedir que alguien se cambie su propio rol o se desactive a sí mismo
        if (usuarioLogueado.getId().equals(id)) {
            if (empleadoActualizado.getRol() != empleadoExistente.getRol()) {
                return ResponseEntity.status(403).body("No puedes cambiar tu propio rol.");
            }
            if (!empleadoActualizado.isActivo() && empleadoExistente.isActivo()) {
                return ResponseEntity.status(403).body("No puedes desactivar tu propia cuenta.");
            }
        }

        // Restricciones para el rol RRHH
        if (usuarioLogueado.getRol() == Rol.RRHH) {
            if (empleadoActualizado.getRol() != empleadoExistente.getRol() ||
                    (empleadoActualizado.getPin() != null && !empleadoActualizado.getPin().equals(empleadoExistente.getPin())) ||
                    empleadoActualizado.isActivo() != empleadoExistente.isActivo() ||
                    (empleadoActualizado.getDocumento() != null && !empleadoActualizado.getDocumento().equals(empleadoExistente.getDocumento()))) {

                return ResponseEntity.status(403).body("RRHH tiene restricciones en campos críticos (Rol, PIN, Estado, Documento).");
            }
            // Aseguramos que no se cambien aunque vengan en el JSON
            empleadoActualizado.setRol(empleadoExistente.getRol());
            empleadoActualizado.setPin(empleadoExistente.getPin());
            empleadoActualizado.setActivo(empleadoExistente.isActivo());
            empleadoActualizado.setDocumento(empleadoExistente.getDocumento());
        }

        // ====== FIN DE VALIDACIONES DE SEGURIDAD ======

        try {
            // Intentar actualizar en el Service (aquí es donde se valida el Email/PIN duplicado)
            Optional<Empleado> actualizado = empleadoService.actualizar(id, empleadoActualizado);

            if (actualizado.isPresent()) {
                // Generar detalle para la auditoría
                String detalle = "Actualización de datos: " + empleadoExistente.getNombre();

                auditoriaService.registrar(
                        "EMPLEADO",
                        id,
                        "EDICIÓN",
                        detalle,
                        usuarioLogueado
                );
                return ResponseEntity.ok(actualizado.get());
            }
            return ResponseEntity.notFound().build();

        } catch (RuntimeException e) {
            // Si el Service lanza la excepción de "Email ya registrado", cae aquí:
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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

        auditoriaService.registrar(
                "EMPLEADO",
                id,
                "ELIMINACIÓN",
                String.format("Se eliminó permanentemente al empleado: %s (Documento: %s)",
                        usuarioLogueado.getNombre(),
                        usuarioLogueado.getDocumento()),
                usuarioLogueado
        );
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
