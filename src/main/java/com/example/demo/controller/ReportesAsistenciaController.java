package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.service.AsistenciaService;
import com.example.demo.service.TurnoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReportesAsistenciaController {

    private final AsistenciaService asistenciaService;
    private final EmpleadoRepository empleadoRepository;
    private final PermisoRepository permisoRepository;
    private final VacacionRepository vacacionRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final TurnoService turnoService;

    /**
     * Vista de reporte de asistencias con TODOS los empleados
     * Muestra: asistencias, ausencias, permisos y vacaciones
     */
    @GetMapping("/asistencias")
    public String verReporteAsistencias(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) Long empleadoId,
            @RequestParam(required = false) String estado,
            HttpSession session,
            Model model) {

        Empleado usuarioLogueado = (Empleado) session.getAttribute("empleado");

        if (usuarioLogueado == null) {
            return "redirect:/login";
        }

        // Solo ADMIN y RRHH pueden ver reportes
        if (!"ADMIN".equals(usuarioLogueado.getRol().toString()) &&
                !"RRHH".equals(usuarioLogueado.getRol().toString())) {
            return "redirect:/index";
        }

        // Si no se especifican fechas, usar hoy
        if (fechaDesde == null) {
            fechaDesde = LocalDate.now();
        }
        if (fechaHasta == null) {
            fechaHasta = fechaDesde;
        }

        // Obtener todos los empleados activos
        List<Empleado> empleados = empleadoRepository.findAll()
                .stream()
                .filter(Empleado::isActivo)
                .sorted((a, b) -> a.getNombre().compareTo(b.getNombre()))
                .collect(Collectors.toList());

        // Crear lista de registros para el reporte
        List<RegistroReporte> registros = new ArrayList<>();

        // Iterar por cada día del rango
        LocalDate fechaActual = fechaDesde;
        while (!fechaActual.isAfter(fechaHasta)) {
            for (Empleado emp : empleados) {
                // Filtrar por empleado si se especifica
                if (empleadoId != null && !emp.getId().equals(empleadoId)) {
                    continue;
                }

                RegistroReporte registro = new RegistroReporte();
                registro.setEmpleado(emp);
                registro.setFecha(fechaActual);

                // Obtener turno del día para horarios esperados
                Turno turnoDelDia = turnoService.obtenerTurnoEmpleado(emp.getId(), fechaActual);

                if (turnoDelDia != null) {
                    registro.setHoraEntradaEsperada(turnoDelDia.getHoraEntrada());
                    registro.setHoraSalidaEsperada(turnoDelDia.getHoraSalida());
                } else if (emp.getHorario() != null) {
                    registro.setHoraEntradaEsperada(emp.getHorario().getHoraEntrada());
                    registro.setHoraSalidaEsperada(emp.getHorario().getHoraSalida());
                }

                // Buscar asistencia
                Optional<Asistencia> asistenciaOpt = asistenciaRepository
                        .findByEmpleadoAndFecha(emp, fechaActual);

                if (asistenciaOpt.isPresent()) {
                    // TIENE ASISTENCIA MARCADA
                    Asistencia asist = asistenciaOpt.get();
                    registro.setEstado(asist.getEstado().toString());
                    registro.setHoraEntrada(asist.getHoraEntrada());
                    registro.setHoraSalida(asist.getHoraSalida());
                    registro.setObservacion(asist.getObservacion());
                    registro.setTipo("ASISTENCIA");

                    // Calcular horas extras
                    if (asist.getHoraSalida() != null && registro.getHoraSalidaEsperada() != null) {
                        if (asist.getHoraSalida().isAfter(registro.getHoraSalidaEsperada())) {
                            long minutos = Duration.between(registro.getHoraSalidaEsperada(), asist.getHoraSalida()).toMinutes();
                            registro.setHorasExtras(minutos / 60.0);
                        }
                    }
                } else {
                    // NO TIENE ASISTENCIA, verificar permisos y vacaciones
                    boolean tienePermiso = permisoRepository.existsPermisoActivo(emp, fechaActual);
                    boolean tieneVacaciones = vacacionRepository.existsVacacionActiva(emp, fechaActual);

                    if (tienePermiso) {
                        registro.setEstado("PERMISO");
                        registro.setTipo("PERMISO");
                    } else if (tieneVacaciones) {
                        registro.setEstado("VACACIONES");
                        registro.setTipo("VACACIONES");
                    } else {
                        // No tiene nada registrado
                        registro.setEstado("AUSENTE");
                        registro.setTipo("AUSENTE");
                    }
                }

                // Filtrar por estado si se especifica
                if (estado != null && !estado.isEmpty() && !estado.equals("TODOS")) {
                    if (!registro.getEstado().equals(estado)) {
                        continue;
                    }
                }

                registros.add(registro);
            }
            fechaActual = fechaActual.plusDays(1);
        }

        // Calcular estadísticas
        long totalNormal = registros.stream().filter(r -> "NORMAL".equals(r.getEstado())).count();
        long totalTarde = registros.stream().filter(r -> "TARDE".equals(r.getEstado())).count();
        long totalAusente = registros.stream().filter(r -> "AUSENTE".equals(r.getEstado())).count();
        long totalPermiso = registros.stream().filter(r -> "PERMISO".equals(r.getEstado())).count();
        long totalVacaciones = registros.stream().filter(r -> "VACACIONES".equals(r.getEstado())).count();

        model.addAttribute("registros", registros);
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);
        model.addAttribute("fechaSeleccionada", fechaDesde);
        model.addAttribute("empleadoSeleccionado", empleadoId);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("empleados", empleados);

        // Estadísticas
        model.addAttribute("totalNormal", totalNormal);
        model.addAttribute("totalTarde", totalTarde);
        model.addAttribute("totalAusente", totalAusente);
        model.addAttribute("totalPermiso", totalPermiso);
        model.addAttribute("totalVacaciones", totalVacaciones);

        return "reportes-asistencias";
    }

    /**
     * Clase auxiliar para el reporte
     */
    public static class RegistroReporte {
        private Empleado empleado;
        private LocalDate fecha;
        private String estado;
        private String tipo;
        private LocalTime horaEntrada;
        private LocalTime horaSalida;
        private LocalTime horaEntradaEsperada;
        private LocalTime horaSalidaEsperada;
        private Double horasExtras;
        private String observacion;

        // Getters y Setters
        public Empleado getEmpleado() { return empleado; }
        public void setEmpleado(Empleado empleado) { this.empleado = empleado; }

        public LocalDate getFecha() { return fecha; }
        public void setFecha(LocalDate fecha) { this.fecha = fecha; }

        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }

        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }

        public LocalTime getHoraEntrada() { return horaEntrada; }
        public void setHoraEntrada(LocalTime horaEntrada) { this.horaEntrada = horaEntrada; }

        public LocalTime getHoraSalida() { return horaSalida; }
        public void setHoraSalida(LocalTime horaSalida) { this.horaSalida = horaSalida; }

        public LocalTime getHoraEntradaEsperada() { return horaEntradaEsperada; }
        public void setHoraEntradaEsperada(LocalTime horaEntradaEsperada) { this.horaEntradaEsperada = horaEntradaEsperada; }

        public LocalTime getHoraSalidaEsperada() { return horaSalidaEsperada; }
        public void setHoraSalidaEsperada(LocalTime horaSalidaEsperada) { this.horaSalidaEsperada = horaSalidaEsperada; }

        public Double getHorasExtras() { return horasExtras; }
        public void setHorasExtras(Double horasExtras) { this.horasExtras = horasExtras; }

        public String getObservacion() {
            return observacion;
        }

        public void setObservacion(String observacion) {
            this.observacion = observacion;
        }
    }
}
