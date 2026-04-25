package com.example.demo.controller;

import com.example.demo.model.AsignacionTurno;
import com.example.demo.model.Empleado;
import com.example.demo.model.Turno;
import com.example.demo.repository.TurnoRepository;
import com.example.demo.service.EmpleadoService;
import com.example.demo.service.TurnoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cuadrantes")
@RequiredArgsConstructor
public class CuadranteController {

    private final TurnoService turnoService;
    private final EmpleadoService empleadoService;
    private final TurnoRepository turnoRepository;

    /**
     * Vista principal del cuadrante mensual
     */
    @GetMapping
    public String verCuadrante(@RequestParam(required = false) Integer mes,
                               @RequestParam(required = false) Integer anio,
                               @RequestParam(required = false) Long empleadoId,
                               HttpSession session,
                               Model model) {

        Empleado empleadoLogueado = (Empleado) session.getAttribute("empleado");

        if (empleadoLogueado == null) {
            return "redirect:/login";
        }

        // Mes y año actuales por defecto
        YearMonth mesActual = YearMonth.now();
        if (mes == null) mes = mesActual.getMonthValue();
        if (anio == null) anio = mesActual.getYear();

        // Si es empleado normal, solo ve su propio cuadrante
        if ("EMPLEADO".equals(empleadoLogueado.getRol().toString())) {
            empleadoId = empleadoLogueado.getId();
        }

        // Cargar datos
        List<Empleado> empleados = empleadoService.listarActivos();
        List<Turno> turnos = turnoRepository.findAll();

        // Si hay empleado seleccionado, cargar su cuadrante
        Map<Integer, AsignacionTurno> cuadrante = new HashMap<>();
        if (empleadoId != null) {
            List<AsignacionTurno> asignaciones = turnoService.obtenerCuadranteMensual(
                    empleadoId, mes, anio
            );

            for (AsignacionTurno asignacion : asignaciones) {
                cuadrante.put(asignacion.getFecha().getDayOfMonth(), asignacion);
            }
        }

        // Días del mes
        YearMonth yearMonth = YearMonth.of(anio, mes);
        int diasEnMes = yearMonth.lengthOfMonth();

        model.addAttribute("empleado", empleadoLogueado);
        model.addAttribute("empleados", empleados);
        model.addAttribute("turnos", turnos);
        model.addAttribute("mesSeleccionado", mes);
        model.addAttribute("anioSeleccionado", anio);
        model.addAttribute("empleadoSeleccionado", empleadoId);
        model.addAttribute("cuadrante", cuadrante);
        model.addAttribute("diasEnMes", diasEnMes);

        // Solo ponemos /cuadrantes si nadie ha puesto otra URL antes
        if (!model.containsAttribute("urlFormulario")) {
            model.addAttribute("urlFormulario", "/cuadrantes");
        }
        return "cuadrantes/ver";
    }

    @GetMapping("/mi-horario")
    public String verMiPropioHorario(HttpSession session, Model model,
                                     @RequestParam(required = false) Integer mes,
                                     @RequestParam(required = false) Integer anio) {
        Empleado empleadoLogueado = (Empleado) session.getAttribute("empleado");
        if (empleadoLogueado == null) return "redirect:/login";

        // Pasamos una bandera para ocultar los controles de gestión
        model.addAttribute("esVistaPersonal", true);

        model.addAttribute("urlFormulario", "/cuadrantes/mi-horario");

        return verCuadrante(mes, anio, empleadoLogueado.getId(), session, model);
    }

    /**
     * Asignar turno a un día específico
     */
    @PostMapping("/asignar-dia")
    @ResponseBody
    public Map<String, Object> asignarTurnoDia(@RequestParam Long empleadoId,
                                               @RequestParam Long turnoId,
                                               @RequestParam String fecha,
                                               HttpSession session) {

        Empleado empleadoLogueado = (Empleado) session.getAttribute("empleado");

        if (empleadoLogueado == null) {
            return Map.of("success", false, "message", "No autenticado");
        }
        try {
            LocalDate fechaLocal = LocalDate.parse(fecha);
            turnoService.asignarTurnoDia(empleadoId, turnoId, fechaLocal);

            // No devuelvas la entidad, solo un mapa simple de confirmación
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            return response;
        } catch (Exception e) {
            e.printStackTrace(); // Esto imprimirá el error real en la consola de IntelliJ
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    /**
     * Formulario para asignar patrón cíclico
     */
    @GetMapping("/asignar-patron")
    public String formularioPatron(@RequestParam Long empleadoId,
                                   @RequestParam Integer mes,
                                   @RequestParam Integer anio,
                                   HttpSession session,
                                   Model model) {

        Empleado empleadoLogueado = (Empleado) session.getAttribute("empleado");

        if (empleadoLogueado == null) {
            return "redirect:/login";
        }

        Empleado empleado = empleadoService.obtenerPorId(empleadoId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        List<Turno> turnos = turnoRepository.findAll();

        model.addAttribute("empleado", empleadoLogueado);
        model.addAttribute("empleadoAsignar", empleado);
        model.addAttribute("turnos", turnos);
        model.addAttribute("mes", mes);
        model.addAttribute("anio", anio);

        return "cuadrantes/patron";
    }

    /**
     * Guardar patrón cíclico
     */
    @PostMapping("/guardar-patron")
    public String guardarPatron(@RequestParam Long empleadoId,
                                @RequestParam Integer mes,
                                @RequestParam Integer anio,
                                @RequestParam String patron,
                                HttpSession session) {

        Empleado empleadoLogueado = (Empleado) session.getAttribute("empleado");

        if (empleadoLogueado == null) {
            return "redirect:/login";
        }

        try {
            // Parsear patrón: "1,1,1,1,1,0,0" donde 0 = día libre
            List<Long> patronTurnoIds = Arrays.stream(patron.split(","))
                    .map(String::trim)
                    .map(s -> s.equals("0") || s.isEmpty() ? null : Long.parseLong(s))
                    .collect(Collectors.toList());

            turnoService.asignarPatronCiclico(empleadoId, patronTurnoIds, mes, anio);

            return "redirect:/cuadrantes?empleadoId=" + empleadoId +
                    "&mes=" + mes + "&anio=" + anio + "&success=true";

        } catch (Exception e) {
            return "redirect:/cuadrantes?empleadoId=" + empleadoId +
                    "&mes=" + mes + "&anio=" + anio + "&error=true";
        }
    }

    /**
     * Asignar turno fijo para todo el mes
     */
    @PostMapping("/asignar-fijo")
    public String asignarTurnoFijo(@RequestParam Long empleadoId,
                                   @RequestParam Long turnoId,
                                   @RequestParam Integer mes,
                                   @RequestParam Integer anio,
                                   @RequestParam(defaultValue = "false") Boolean incluirFinDeSemana,
                                   HttpSession session) {

        Empleado empleadoLogueado = (Empleado) session.getAttribute("empleado");

        if (empleadoLogueado == null) {
            return "redirect:/login";
        }

        turnoService.asignarTurnoFijoMensual(empleadoId, turnoId, mes, anio, incluirFinDeSemana);

        return "redirect:/cuadrantes?empleadoId=" + empleadoId +
                "&mes=" + mes + "&anio=" + anio + "&success=true";
    }

    /**
     * Limpiar cuadrante del mes
     */
    @PostMapping("/limpiar")
    public String limpiarCuadrante(@RequestParam Long empleadoId,
                                   @RequestParam Integer mes,
                                   @RequestParam Integer anio,
                                   HttpSession session) {

        Empleado empleadoLogueado = (Empleado) session.getAttribute("empleado");

        if (empleadoLogueado == null) {
            return "redirect:/login";
        }

        turnoService.limpiarCuadranteMensual(empleadoId, mes, anio);

        return "redirect:/cuadrantes?empleadoId=" + empleadoId +
                "&mes=" + mes + "&anio=" + anio + "&cleared=true";
    }
}
