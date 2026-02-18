package com.example.demo.controller;

import com.example.demo.model.Asistencia;
import com.example.demo.model.Empleado;
import com.example.demo.service.AsistenciaService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/asistencias")
@RequiredArgsConstructor
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    /**
     * Marcar asistencia del empleado logueado
     * POST /api/asistencias/marcar
     */
    @PostMapping("/marcar")
    public ResponseEntity<?> marcarAsistencia(HttpSession session) {
        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        try {
            Asistencia asistencia = asistenciaService.marcarAsistencia(empleado.getPin());
            return ResponseEntity.ok(asistencia);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Obtener la asistencia de hoy del empleado logueado
     * GET /api/asistencias/mi-asistencia-hoy
     */
    @GetMapping("/mi-asistencia-hoy")
    public ResponseEntity<?> miAsistenciaHoy(HttpSession session) {
        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        return asistenciaService.obtenerAsistenciaHoy(empleado)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Listar todas las asistencias (con filtros opcionales)
     * GET /api/asistencias?fecha=2026-02-15&empleadoId=3&estado=TARDE
     * Solo ADMIN y RRHH pueden acceder
     */
    @GetMapping
    public ResponseEntity<?> listarAsistencias(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Long empleadoId,
            @RequestParam(required = false) String estado,
            HttpSession session) {

        Empleado usuarioLogueado = (Empleado) session.getAttribute("empleado");

        if (usuarioLogueado == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        // Validar que sea ADMIN o RRHH
        if (!"ADMIN".equals(usuarioLogueado.getRol().toString()) &&
                !"RRHH".equals(usuarioLogueado.getRol().toString())) {
            return ResponseEntity.status(403).body("Solo ADMIN y RRHH pueden ver reportes");
        }

        List<Asistencia> asistencias;

        // Aplicar filtros
        if (fecha != null && empleadoId != null) {
            asistencias = asistenciaService.listarPorFechaYEmpleado(fecha, empleadoId);
        } else if (fecha != null) {
            asistencias = asistenciaService.listarPorFecha(fecha);
        } else if (empleadoId != null) {
            asistencias = asistenciaService.listarPorEmpleado(empleadoId);
        } else {
            asistencias = asistenciaService.listarTodas();
        }

        // Filtrar por estado si se especifica
        if (estado != null && !estado.isEmpty()) {
            asistencias = asistencias.stream()
                    .filter(a -> a.getEstado().toString().equals(estado))
                    .toList();
        }

        return ResponseEntity.ok(asistencias);
    }

    /**
     * Obtener asistencia por ID
     * GET /api/asistencias/123
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(
            @PathVariable Long id,
            HttpSession session) {

        Empleado usuarioLogueado = (Empleado) session.getAttribute("empleado");

        if (usuarioLogueado == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        return asistenciaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtener historial de asistencias del empleado logueado
     * GET /api/asistencias/mi-historial?mes=2&anio=2026
     */
    @GetMapping("/mi-historial")
    public ResponseEntity<?> miHistorial(
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer anio,
            HttpSession session) {

        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        List<Asistencia> historial;

        if (mes != null && anio != null) {
            historial = asistenciaService.listarPorEmpleadoYMes(empleado.getId(), mes, anio);
        } else {
            historial = asistenciaService.listarPorEmpleado(empleado.getId());
        }

        return ResponseEntity.ok(historial);
    }

    /**
     * Estadísticas del empleado logueado
     * GET /api/asistencias/mis-estadisticas
     */
    @GetMapping("/mis-estadisticas")
    public ResponseEntity<?> misEstadisticas(HttpSession session) {
        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        // Aquí puedes crear un DTO con estadísticas
        // Por ahora retornamos info básica
        return ResponseEntity.ok().build();
    }
}

