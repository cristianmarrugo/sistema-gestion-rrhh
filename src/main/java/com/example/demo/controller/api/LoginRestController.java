package com.example.demo.controller.api;

import com.example.demo.model.Empleado;
import com.example.demo.repository.AsignacionTurnoRepository;
import com.example.demo.repository.AsistenciaRepository;
import com.example.demo.repository.EmpleadoRepository;
import com.example.demo.repository.PermisoRepository;
import com.example.demo.repository.VacacionRepository;
import com.example.demo.service.AsistenciaService;
import com.example.demo.service.EmpleadoService;
import com.example.demo.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LoginRestController {

    private final EmpleadoService empleadoService;
    private final AsistenciaService asistenciaService;
    private final AsistenciaRepository asistenciaRepository;
    private final PermisoRepository permisoRepository;
    private final VacacionRepository vacacionRepository;
    private final EmpleadoRepository empleadoRepository;
    private final AsignacionTurnoRepository asignacionTurnoRepository;
    private final NotificacionService notificacionService;

    // 1. PROCESAR LOGIN DESDE EL CELULAR
    @PostMapping("/login")
    public Map<String, Object> procesarLogin(@RequestParam String documento, @RequestParam String pin) {
        Map<String, Object> response = new HashMap<>();
        Empleado empleado = empleadoService.buscarPorDocumento(documento);

        if (empleado == null) {
            response.put("success", false);
            response.put("message", "Usuario no encontrado");
            return response;
        }

        if (!empleado.isActivo()) {
            response.put("success", false);
            response.put("message", "Usuario inactivo. Contacte con RRHH");
            return response;
        }

        if (!empleado.getPin().equals(pin)) {
            response.put("success", false);
            response.put("message", "PIN incorrecto");
            return response;
        }

        // Login Exitoso: Le mandamos a Flutter toda la info del usuario y sus notificaciones
        response.put("success", true);
        response.put("empleado", empleado);
        response.put("notificacionesNoLeidas", notificacionService.listarNoLeidas(empleado));
        return response;
    }

    // 2. OBTENER ESTADÍSTICAS DEL HOME (Solo para perfiles Admin/RRHH en el móvil)
    @GetMapping("/dashboard-stats")
    public Map<String, Object> obtenerEstadisticasHome() {
        Map<String, Object> model = new HashMap<>();
        LocalDate hoy = LocalDate.now();

        var stats = asistenciaService.obtenerEstadisticas(hoy, hoy, null, "TODOS");
        long totalNormal = stats.getTotalNormal();
        long totalTarde = stats.getTotalTarde();

        long totalPermiso = permisoRepository.countPermisosActivos(hoy);
        long totalVacaciones = vacacionRepository.countVacacionesActivas(hoy);

        long totalDescanso = empleadoRepository.findAll().stream()
                .filter(Empleado::isActivo)
                .filter(emp -> {
                    boolean sinTurno = asignacionTurnoRepository.findByEmpleadoIdAndFecha(emp.getId(), hoy).isEmpty();
                    boolean sinVacaciones = !vacacionRepository.existsVacacionActiva(emp, hoy);
                    boolean sinPermiso = !permisoRepository.existsPermisoActivo(emp, hoy);
                    return sinTurno && sinVacaciones && sinPermiso;
                })
                .count();

        long totalEmpleadosActivos = empleadoRepository.countByActivoTrue();
        long totalAusente = totalEmpleadosActivos - (totalNormal + totalTarde + totalPermiso + totalVacaciones + totalDescanso);
        if (totalAusente < 0) totalAusente = 0;

        model.put("totalNormal", totalNormal);
        model.put("totalTarde", totalTarde);
        model.put("totalPermiso", totalPermiso);
        model.put("totalVacaciones", totalVacaciones);
        model.put("totalDescanso", totalDescanso);
        model.put("totalAusente", totalAusente);
        model.put("recientes", asistenciaRepository.findTop5ByFechaOrderByHoraEntradaDesc(hoy));

        return model;
    }
}
