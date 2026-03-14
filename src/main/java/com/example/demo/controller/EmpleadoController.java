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

        // 1. Corregimos la variable: usuarioLogueado es quien realiza la acción
        Empleado usuarioLogueado = (Empleado) session.getAttribute("empleado");

        if (usuarioLogueado == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        // Obtener el empleado actual de la BD para comparar cambios
        Empleado empleadoExistente = empleadoService.obtenerPorId(id).orElse(null);

        if (empleadoExistente == null) {
            return ResponseEntity.notFound().build();
        }

        // [Tus validaciones de seguridad se mantienen igual...]
        if (usuarioLogueado.getId().equals(id)) {
            if (empleadoActualizado.getRol() != empleadoExistente.getRol()) {
                return ResponseEntity.status(403).body("No puedes cambiar tu propio rol.");
            }
            if (!empleadoActualizado.isActivo() && empleadoExistente.isActivo()) {
                return ResponseEntity.status(403).body("No puedes desactivar tu propia cuenta.");
            }
        }

        // ====== VALIDACIÓN DE PERMISOS PARA RRHH ======
        if (usuarioLogueado.getRol() == Rol.RRHH) {
            if (empleadoActualizado.getRol() != empleadoExistente.getRol() ||
                    (empleadoActualizado.getPin() != null && !empleadoActualizado.getPin().equals(empleadoExistente.getPin())) ||
                    empleadoActualizado.isActivo() != empleadoExistente.isActivo() ||
                    (empleadoActualizado.getDocumento() != null && !empleadoActualizado.getDocumento().equals(empleadoExistente.getDocumento()))) {

                return ResponseEntity.status(403).body("RRHH tiene restricciones en campos críticos.");
            }

            // Forzar valores originales
            empleadoActualizado.setRol(empleadoExistente.getRol());
            empleadoActualizado.setPin(empleadoExistente.getPin());
            empleadoActualizado.setActivo(empleadoExistente.isActivo());
            empleadoActualizado.setDocumento(empleadoExistente.getDocumento());
        }

        // ====== LÓGICA DE AUDITORÍA MEJORADA ======
        StringBuilder detalle = new StringBuilder("Cambios realizados: ");
        if (!empleadoExistente.getNombre().equals(empleadoActualizado.getNombre())) {
            detalle.append(String.format("Nombre (%s -> %s) ", empleadoExistente.getNombre(), empleadoActualizado.getNombre()));
        }

        // Si no hubo cambios específicos detectados, dejamos un mensaje genérico
        String detalleFinal = detalle.length() > 20 ? detalle.toString() : "Actualización de datos generales del empleado: " + empleadoExistente.getNombre();

        // AQUÍ CORREGIMOS EL ERROR: cambiamos 'admin' por 'usuarioLogueado'
        auditoriaService.registrar(
                "EMPLEADO",
                id,
                "EDICIÓN",
                detalleFinal,
                usuarioLogueado
        );

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
