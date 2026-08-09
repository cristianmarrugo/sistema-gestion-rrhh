package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.model.Asistencia;
import com.example.demo.model.Empleado;
import com.example.demo.service.AsistenciaService;
import com.example.demo.service.EmpleadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/powerbi")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PowerBIDataController {

    private final AsistenciaService asistenciaService;
    private final EmpleadoService empleadoService;

    /**
     * Datos completos de asistencias para Power BI
     * GET /api/powerbi/asistencias?desde=2026-01-01&hasta=2026-12-31
     */
    @GetMapping("/asistencias")
    public ResponseEntity<List<AsistenciaDTO>> getAsistenciasParaPowerBI(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        // ⭐ SIN VALIDACIÓN DE PERMISOS - Para Power BI Desktop

        List<AsistenciaDTO> datos = asistenciaService.listarPorRango(desde, hasta)
                .stream()
                .map(a -> new AsistenciaDTO(
                        a.getFecha(),
                        a.getEmpleado().getNombre() + " " + a.getEmpleado().getApellido(),
                        a.getEmpleado().getDocumento(),
                        a.getEmpleado().getCargo() != null ? a.getEmpleado().getCargo().getNombre() : "Sin cargo",
                        a.getHoraEntrada(),
                        a.getHoraSalida(),
                        a.getEstado().toString(),
                        calcularHorasTrabajadas(a),
                        a.getFecha().getYear(),
                        a.getFecha().getMonthValue(),
                        getNombreMes(a.getFecha().getMonthValue()),
                        a.getFecha().getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es")),
                        a.getFecha().getDayOfWeek().getValue()
                ))
                .toList();

        return ResponseEntity.ok(datos);
    }

    /**
     * Resumen mensual agregado
     * GET /api/powerbi/resumen-mensual?anio=2026
     */
    @GetMapping("/resumen-mensual")
    public ResponseEntity<List<ResumenMensualDTO>> getResumenMensual(@RequestParam int anio) {

        // ⭐ SIN VALIDACIÓN DE PERMISOS

        List<ResumenMensualDTO> resumen = new ArrayList<>();

        for (int mes = 1; mes <= 12; mes++) {
            List<Asistencia> asistencias = asistenciaService.listarPorMes(mes, anio);

            long total = asistencias.size();
            long normal = asistencias.stream().filter(a -> a.getEstado().toString().equals("NORMAL")).count();
            long tarde = asistencias.stream().filter(a -> a.getEstado().toString().equals("TARDE")).count();
            long ausente = asistencias.stream().filter(a -> a.getEstado().toString().equals("AUSENTE")).count();
            long permiso = asistencias.stream().filter(a -> a.getEstado().toString().equals("PERMISO")).count();

            double porcentajePuntualidad = total > 0 ? (normal * 100.0 / total) : 0;

            resumen.add(new ResumenMensualDTO(
                    anio,
                    mes,
                    getNombreMes(mes),
                    total,
                    normal,
                    tarde,
                    ausente,
                    permiso,
                    Math.round(porcentajePuntualidad * 100.0) / 100.0
            ));
        }

        return ResponseEntity.ok(resumen);
    }

    /**
     * Análisis de tardanzas por empleado
     * GET /api/powerbi/analisis-tardanzas?desde=2026-01-01&hasta=2026-12-31
     */
    @GetMapping("/analisis-tardanzas")
    public ResponseEntity<List<EmpleadoTardanzasDTO>> getAnalisisTardanzas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        // ⭐ SIN VALIDACIÓN DE PERMISOS

        List<Asistencia> asistencias = asistenciaService.listarPorRango(desde, hasta);

        // Agrupar por empleado
        Map<Empleado, List<Asistencia>> porEmpleado = asistencias.stream()
                .collect(Collectors.groupingBy(Asistencia::getEmpleado));

        List<EmpleadoTardanzasDTO> analisis = porEmpleado.entrySet().stream()
                .map(entry -> {
                    Empleado emp = entry.getKey();
                    List<Asistencia> asists = entry.getValue();

                    long total = asists.size();
                    long tardanzas = asists.stream().filter(a -> a.getEstado().toString().equals("TARDE")).count();
                    long ausencias = asists.stream().filter(a -> a.getEstado().toString().equals("AUSENTE")).count();

                    double porcentajeTardanzas = total > 0 ? (tardanzas * 100.0 / total) : 0;

                    return new EmpleadoTardanzasDTO(
                            emp.getNombre() + " " + emp.getApellido(),
                            emp.getDocumento(),
                            emp.getCargo() != null ? emp.getCargo().getNombre() : "Sin cargo",
                            total,
                            tardanzas,
                            ausencias,
                            Math.round(porcentajeTardanzas * 100.0) / 100.0
                    );
                })
                .sorted((a, b) -> Long.compare(b.totalTardanzas(), a.totalTardanzas()))
                .toList();

        return ResponseEntity.ok(analisis);
    }

    /**
     * Lista de empleados activos
     * GET /api/powerbi/empleados
     */
    @GetMapping("/empleados")
    public ResponseEntity<List<Map<String, Object>>> getEmpleados() {

        // ⭐ SIN VALIDACIÓN DE PERMISOS

        List<Map<String, Object>> datos = empleadoService.listarTodos().stream()
                .map(emp -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("nombre", emp.getNombre() + " " + emp.getApellido());
                    map.put("documento", emp.getDocumento());
                    map.put("email", emp.getEmail());
                    map.put("cargo", emp.getCargo() != null ? emp.getCargo().getNombre() : "Sin cargo");
                    map.put("salario", emp.getCargo() != null ? emp.getCargo().getSalarioBase() : 0);
                    map.put("fechaIngreso", emp.getFechaIngreso());
                    map.put("activo", emp.isActivo());
                    map.put("rol", emp.getRol().toString());
                    return map;
                })
                .toList();

        return ResponseEntity.ok(datos);
    }

    // ====== HELPERS ======

    private double calcularHorasTrabajadas(Asistencia a) {
        if (a.getHoraEntrada() == null || a.getHoraSalida() == null) {
            return 0;
        }
        long minutos = Duration.between(a.getHoraEntrada(), a.getHoraSalida()).toMinutes();
        return Math.round(minutos / 60.0 * 100.0) / 100.0;
    }

    private String getNombreMes(int mes) {
        String[] meses = {
                "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        };
        return mes >= 1 && mes <= 12 ? meses[mes - 1] : "Mes inválido";
    }
}
