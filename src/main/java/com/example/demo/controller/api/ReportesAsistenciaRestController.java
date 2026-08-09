package com.example.demo.controller.api;

import com.example.demo.controller.ReportesAsistenciaController.RegistroReporte;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.service.TurnoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReportesAsistenciaRestController {

    private final EmpleadoRepository empleadoRepository;
    private final PermisoRepository permisoRepository;
    private final VacacionRepository vacacionRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final TurnoService turnoService;

    // Flutter llamará a: GET http://tu-ip:8080/api/reportes/asistencias?fechaDesde=2026-05-01&fechaHasta=2026-05-15
    @GetMapping("/asistencias")
    public Map<String, Object> obtenerReporteAsistencias(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) Long empleadoId,
            @RequestParam(required = false) String estado) {

        Map<String, Object> response = new HashMap<>();

        if (fechaDesde == null) fechaDesde = LocalDate.now();
        if (fechaHasta == null) fechaHasta = fechaDesde;

        List<Empleado> empleados = empleadoRepository.findAll()
                .stream()
                .filter(Empleado::isActivo)
                .sorted(Comparator.comparing(Empleado::getNombre))
                .collect(Collectors.toList());

        List<RegistroReporte> registros = new ArrayList<>();

        LocalDate fechaActual = fechaDesde;
        while (!fechaActual.isAfter(fechaHasta)) {
            for (Empleado emp : empleados) {
                if (empleadoId != null && !emp.getId().equals(empleadoId)) {
                    continue;
                }

                RegistroReporte registro = new RegistroReporte();
                registro.setEmpleado(emp);
                registro.setFecha(fechaActual);

                Turno turnoDelDia = turnoService.obtenerTurnoEmpleado(emp.getId(), fechaActual);

                if (turnoDelDia != null) {
                    registro.setHoraEntradaEsperada(turnoDelDia.getHoraEntrada());
                    registro.setHoraSalidaEsperada(turnoDelDia.getHoraSalida());
                } else if (emp.getHorario() != null) {
                    registro.setHoraEntradaEsperada(emp.getHorario().getHoraEntrada());
                    registro.setHoraSalidaEsperada(emp.getHorario().getHoraSalida());
                }

                Optional<Asistencia> asistenciaOpt = asistenciaRepository.findByEmpleadoAndFecha(emp, fechaActual);

                if (asistenciaOpt.isPresent()) {
                    Asistencia asist = asistenciaOpt.get();
                    registro.setEstado(asist.getEstado().toString());
                    registro.setHoraEntrada(asist.getHoraEntrada());
                    registro.setHoraSalida(asist.getHoraSalida());
                    registro.setObservacion(asist.getObservacion());
                    registro.setTipo("ASISTENCIA");

                    if (asist.getHoraSalida() != null && registro.getHoraSalidaEsperada() != null) {
                        if (asist.getHoraSalida().isAfter(registro.getHoraSalidaEsperada())) {
                            long minutos = Duration.between(registro.getHoraSalidaEsperada(), asist.getHoraSalida()).toMinutes();
                            registro.setHorasExtras(minutos / 60.0);
                        }
                    }
                } else {
                    boolean tienePermiso = permisoRepository.existsPermisoActivo(emp, fechaActual);
                    boolean tieneVacaciones = vacacionRepository.existsVacacionActiva(emp, fechaActual);

                    if (tienePermiso) {
                        registro.setEstado("PERMISO");
                        registro.setTipo("PERMISO");
                    } else if (tieneVacaciones) {
                        registro.setEstado("VACACIONES");
                        registro.setTipo("VACACIONES");
                    } else {
                        if (turnoDelDia != null) {
                            registro.setEstado("AUSENTE");
                            registro.setTipo("AUSENTE");
                        } else {
                            registro.setEstado("DESCANSO");
                            registro.setTipo("DESCANSO");
                        }
                    }
                }

                if (estado != null && !estado.isEmpty() && !estado.equals("TODOS")) {
                    if (!registro.getEstado().equals(estado)) {
                        continue;
                    }
                }

                registros.add(registro);
            }
            fechaActual = fechaActual.plusDays(1);
        }

        // Serializamos los totales y la lista a un mapa JSON limpio
        response.put("registros", registros);
        response.put("totalNormal", registros.stream().filter(r -> "NORMAL".equals(r.getEstado())).count());
        response.put("totalTarde", registros.stream().filter(r -> "TARDE".equals(r.getEstado())).count());
        response.put("totalAusente", registros.stream().filter(r -> "AUSENTE".equals(r.getEstado())).count());
        response.put("totalPermiso", registros.stream().filter(r -> "PERMISO".equals(r.getEstado())).count());
        response.put("totalVacaciones", registros.stream().filter(r -> "VACACIONES".equals(r.getEstado())).count());
        response.put("totalDescanso", registros.stream().filter(r -> "DESCANSO".equals(r.getEstado())).count());

        return response;
    }
}
