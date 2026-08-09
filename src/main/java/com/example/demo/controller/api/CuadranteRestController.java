package com.example.demo.controller.api;

import com.example.demo.model.AsignacionTurno;
import com.example.demo.model.Empleado;
import com.example.demo.model.Turno;
import com.example.demo.repository.TurnoRepository;
import com.example.demo.service.EmpleadoService;
import com.example.demo.service.TurnoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cuadrantes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CuadranteRestController {

    private final TurnoService turnoService;
    private final EmpleadoService empleadoService;
    private final TurnoRepository turnoRepository;

    // Obtener los datos del cuadrante para el calendario de Flutter
    @GetMapping
    public Map<String, Object> obtenerCuadrante(
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer anio,
            @RequestParam Long empleadoId) { // Flutter envía el ID del empleado seleccionado o logueado

        YearMonth mesActual = YearMonth.now();
        if (mes == null) mes = mesActual.getMonthValue();
        if (anio == null) anio = mesActual.getYear();

        Map<Integer, AsignacionTurno> cuadrante = new HashMap<>();
        List<AsignacionTurno> asignaciones = turnoService.obtenerCuadranteMensual(empleadoId, mes, anio);

        for (AsignacionTurno asignacion : asignaciones) {
            cuadrante.put(asignacion.getFecha().getDayOfMonth(), asignacion);
        }

        YearMonth yearMonth = YearMonth.of(anio, mes);

        Map<String, Object> response = new HashMap<>();
        response.put("cuadrante", cuadrante); // Envía los días con turnos
        response.put("diasEnMes", yearMonth.lengthOfMonth());
        response.put("turnosDisponibles", turnoRepository.findAll()); // Para los selectores del móvil
        response.put("empleadosActivos", empleadoService.listarActivos()); // Para asignar a otros si es admin

        return response;
    }

    // Asignar turno a un día específico (Ya devolvía JSON, lo dejamos igual pero con la nueva ruta)
    @PostMapping("/asignar-dia")
    public Map<String, Object> asignarTurnoDia(@RequestParam Long empleadoId,
                                               @RequestParam Long turnoId,
                                               @RequestParam String fecha) {
        try {
            LocalDate fechaLocal = LocalDate.parse(fecha);
            turnoService.asignarTurnoDia(empleadoId, turnoId, fechaLocal);
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    // Guardar patrón cíclico desde el móvil
    @PostMapping("/guardar-patron")
    public Map<String, Object> guardarPatron(@RequestParam Long empleadoId,
                                             @RequestParam Integer mes,
                                             @RequestParam Integer anio,
                                             @RequestParam String patron) {
        try {
            List<Long> patronTurnoIds = Arrays.stream(patron.split(","))
                    .map(String::trim)
                    .map(s -> s.equals("0") || s.isEmpty() ? null : Long.parseLong(s))
                    .collect(Collectors.toList());

            turnoService.asignarPatronCiclico(empleadoId, patronTurnoIds, mes, anio);
            return Map.of("success", true, "message", "Patrón guardado con éxito");
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    // Asignar turno fijo para todo el mes
    @PostMapping("/asignar-fijo")
    public Map<String, Object> asignarTurnoFijo(@RequestParam Long empleadoId,
                                                @RequestParam Long turnoId,
                                                @RequestParam Integer mes,
                                                @RequestParam Integer anio,
                                                @RequestParam(defaultValue = "false") Boolean incluirFinDeSemana) {
        try {
            turnoService.asignarTurnoFijoMensual(empleadoId, turnoId, mes, anio, incluirFinDeSemana);
            return Map.of("success", true, "message", "Turno fijo asignado");
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    // Limpiar cuadrante del mes
    @PostMapping("/limpiar")
    public Map<String, Object> limpiarCuadrante(@RequestParam Long empleadoId,
                                                @RequestParam Integer mes,
                                                @RequestParam Integer anio) {
        try {
            turnoService.limpiarCuadranteMensual(empleadoId, mes, anio);
            return Map.of("success", true, "message", "Cuadrante limpio");
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
}