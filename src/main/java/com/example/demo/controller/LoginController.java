package com.example.demo.controller;

import com.example.demo.model.Empleado;
import com.example.demo.repository.*;
import com.example.demo.service.AsistenciaService;
import com.example.demo.service.EmpleadoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final EmpleadoService empleadoService;

    private final AsistenciaService asistenciaService;

    private final AsistenciaRepository asistenciaRepository;

    private final PermisoRepository permisoRepository;

    private final VacacionRepository vacacionRepository;

    private final EmpleadoRepository empleadoRepository;

    private final AsignacionTurnoRepository asignacionTurnoRepository;

    @GetMapping("/")
    public String home() {


        return "redirect:/pin";
    }

    @GetMapping("/login")
    public String mostrarLogin(@RequestParam(required = false) String error,
                               @RequestParam(required = false) String logout,
                               Model model) {
        if (error != null) {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
        }
        if (logout != null) {
            model.addAttribute("mensaje", "Sesión cerrada correctamente");
        }
        return "pin";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String documento,
                                @RequestParam String pin,
                                HttpSession session,
                                Model model) {

        Empleado empleado = empleadoService.buscarPorDocumento(documento);

        if (empleado == null) {
            model.addAttribute("error", "Usuario no encontrado");
            return "pin";
        }

        if (!empleado.isActivo()) {
            model.addAttribute("error", "Usuario inactivo. Contacte con RRHH");
            return "pin";
        }

        if (!empleado.getPin().equals(pin)) {
            model.addAttribute("error", "PIN incorrecto");
            return "pin";
        }

        model.addAttribute("rol", session.getAttribute("rol"));

        // Login exitoso - guardar en sesión
        session.setAttribute("empleado", empleado);
        return "redirect:/index";
    }

    @GetMapping("/logout")
    public String cerrarSesion(HttpSession session) {
        session.invalidate();
        return "redirect:/pin?logout=true";
    }

    @GetMapping("/index")
    public String index(HttpSession session, Model model) {
        // 1. Obtener empleado de la sesión (como lo haces en PowerBI)
        Empleado empleado = (Empleado) session.getAttribute("empleado");
        if (empleado == null) return "redirect:/login";

        model.addAttribute("empleado", empleado);

        // 2. Si no es un simple empleado, cargamos los datos reales de HOY
        if (!"EMPLEADO".equals(empleado.getRol().toString())) {
            LocalDate hoy = LocalDate.now();

            // 1. Datos básicos de asistencias que SÍ existen
            var stats = asistenciaService.obtenerEstadisticas(hoy, hoy, null, "TODOS");
            long totalNormal = stats.getTotalNormal();
            long totalTarde = stats.getTotalTarde();

            // 2. Datos de Permisos y Vacaciones
            long totalPermiso = permisoRepository.countPermisosActivos(hoy);
            long totalVacaciones = vacacionRepository.countVacacionesActivas(hoy);

            // 3. CÁLCULO DE DESCANSOS (Excluyendo a los que ya tienen Permiso o Vacaciones)
            long totalDescanso = empleadoRepository.findAll().stream()
                    .filter(Empleado::isActivo)
                    .filter(emp -> {
                        // No debe tener turno hoy
                        boolean sinTurno = asignacionTurnoRepository.findByEmpleadoIdAndFecha(emp.getId(), hoy).isEmpty();
                        // No debe estar en vacaciones hoy
                        boolean sinVacaciones = !vacacionRepository.existsVacacionActiva(emp, hoy);
                        // No debe tener permiso hoy
                        boolean sinPermiso = !permisoRepository.existsPermisoActivo(emp, hoy);

                        return sinTurno && sinVacaciones && sinPermiso;
                    })
                    .count();

            // 4. CÁLCULO DE AUSENTES
            long totalEmpleadosActivos = empleadoRepository.countByActivoTrue();

            // Ausentes = Total - (Los que vinieron + Los que tienen novedad + Los que descansan)
            long totalAusente = totalEmpleadosActivos - (totalNormal + totalTarde + totalPermiso + totalVacaciones + totalDescanso);

            if (totalAusente < 0) totalAusente = 0;

            // 5. Pasar todo al modelo
            model.addAttribute("totalNormal", totalNormal);
            model.addAttribute("totalTarde", totalTarde);
            model.addAttribute("totalPermiso", totalPermiso);
            model.addAttribute("totalVacaciones", totalVacaciones);
            model.addAttribute("totalDescanso", totalDescanso); // Este ahora será el número real filtrado
            model.addAttribute("totalAusente", totalAusente);

            model.addAttribute("recientes", asistenciaRepository.findTop5ByFechaOrderByHoraEntradaDesc(hoy));
        }

        return "index";
    }
}

