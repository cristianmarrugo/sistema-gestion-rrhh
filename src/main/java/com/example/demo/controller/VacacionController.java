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

    /**
     * Solicitar vacaciones (empleado logueado)
     * POST /api/vacaciones/solicitar
     */
    @PostMapping("/solicitar")
    public ResponseEntity<?> solicitarVacaciones(
            @RequestBody Vacacion vacacion,
            HttpSession session) {

        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        // Validaciones
        if (vacacion.getFechaInicio() == null || vacacion.getFechaFin() == null) {
            return ResponseEntity.badRequest().body("Las fechas son obligatorias");
        }

        if (vacacion.getFechaFin().isBefore(vacacion.getFechaInicio())) {
            return ResponseEntity.badRequest()
                    .body("La fecha fin no puede ser anterior a la fecha inicio");
        }

        if (vacacion.getDiasSolicitados() <= 0) {
            return ResponseEntity.badRequest().body("Días solicitados deben ser mayor a 0");
        }

        if (vacacion.getDiasSolicitados() > 30) {
            return ResponseEntity.badRequest()
                    .body("No puedes solicitar más de 30 días de vacaciones de una vez");
        }

        try {
            Vacacion nuevaVacacion = vacacionService.solicitarVacaciones(empleado, vacacion);
            return ResponseEntity.ok(nuevaVacacion);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Obtener mis vacaciones (empleado logueado)
     * GET /api/vacaciones/mis-vacaciones
     */
    @GetMapping("/mis-vacaciones")
    public ResponseEntity<?> misVacaciones(HttpSession session) {
        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        List<Vacacion> vacaciones = vacacionService.listarPorEmpleado(empleado);
        return ResponseEntity.ok(vacaciones);
    }

    /**
     * Obtener días disponibles (empleado logueado)
     * GET /api/vacaciones/dias-disponibles
     */
    @GetMapping("/dias-disponibles")
    public ResponseEntity<?> diasDisponibles(HttpSession session) {
        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        int dias = vacacionService.diasDisponibles(empleado);
        return ResponseEntity.ok(dias);
    }

    /**
     * Listar todas las vacaciones (ADMIN/RRHH)
     * GET /api/vacaciones
     */
    @GetMapping
    public ResponseEntity<?> listarTodas(HttpSession session) {
        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        // Solo ADMIN y RRHH pueden ver todas
        if (!"ADMIN".equals(empleado.getRol().toString()) &&
                !"RRHH".equals(empleado.getRol().toString())) {
            return ResponseEntity.status(403)
                    .body("Solo ADMIN y RRHH pueden ver todas las vacaciones");
        }

        List<Vacacion> vacaciones = vacacionService.listarTodas();
        return ResponseEntity.ok(vacaciones);
    }

    /**
     * Listar vacaciones pendientes (ADMIN/RRHH)
     * GET /api/vacaciones/pendientes
     */
    @GetMapping("/pendientes")
    public ResponseEntity<?> listarPendientes(HttpSession session) {
        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        // Solo ADMIN y RRHH pueden aprobar
        if (!"ADMIN".equals(empleado.getRol().toString()) &&
                !"RRHH".equals(empleado.getRol().toString())) {
            return ResponseEntity.status(403)
                    .body("Solo ADMIN y RRHH pueden ver vacaciones pendientes");
        }

        List<Vacacion> vacaciones = vacacionService.listarPendientes();
        return ResponseEntity.ok(vacaciones);
    }

    /**
     * Aprobar vacaciones (ADMIN/RRHH)
     * PUT /api/vacaciones/123/aprobar
     */
    @PutMapping("/{id}/aprobar")
    public ResponseEntity<?> aprobar(
            @PathVariable Long id,
            HttpSession session) {

        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        // Solo ADMIN y RRHH pueden aprobar
        if (!"ADMIN".equals(empleado.getRol().toString()) &&
                !"RRHH".equals(empleado.getRol().toString())) {
            return ResponseEntity.status(403)
                    .body("Solo ADMIN y RRHH pueden aprobar vacaciones");
        }

        try {
            Vacacion vacacion = vacacionService.aprobar(id);
            return ResponseEntity.ok(vacacion);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Rechazar vacaciones (ADMIN/RRHH)
     * PUT /api/vacaciones/123/rechazar
     */
    @PutMapping("/{id}/rechazar")
    public ResponseEntity<?> rechazar(
            @PathVariable Long id,
            HttpSession session) {

        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        // Solo ADMIN y RRHH pueden rechazar
        if (!"ADMIN".equals(empleado.getRol().toString()) &&
                !"RRHH".equals(empleado.getRol().toString())) {
            return ResponseEntity.status(403)
                    .body("Solo ADMIN y RRHH pueden rechazar vacaciones");
        }

        try {
            Vacacion vacacion = vacacionService.rechazar(id);
            return ResponseEntity.ok(vacacion);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Obtener vacación por ID
     * GET /api/vacaciones/123
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(
            @PathVariable Long id,
            HttpSession session) {

        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        return vacacionService.obtenerPorId(id)
                .map(vacacion -> {
                    // Verificar que el empleado pueda ver esta vacación
                    boolean esPropio = vacacion.getEmpleado().getId().equals(empleado.getId());
                    boolean esAdminORRHH = "ADMIN".equals(empleado.getRol().toString()) ||
                            "RRHH".equals(empleado.getRol().toString());

                    if (esPropio || esAdminORRHH) {
                        return ResponseEntity.ok(vacacion);
                    } else {
                        return ResponseEntity.status(403)
                                .body("No tienes permiso para ver esta solicitud");
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
