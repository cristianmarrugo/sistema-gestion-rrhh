package com.example.demo.controller;

import com.example.demo.model.Empleado;
import com.example.demo.repository.AsistenciaRepository;
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

            // LLAMADA CLAVE: Usamos la misma lógica de tu reporte
            // Filtramos: desde HOY, hasta HOY, todos los empleados, todos los estados
            var stats = asistenciaService.obtenerEstadisticas(hoy, hoy, null, "TODOS");

            // Pasamos los datos exactos que calculó tu servicio
            model.addAttribute("totalNormal", stats.getTotalNormal());
            model.addAttribute("totalTarde", stats.getTotalTarde());
            model.addAttribute("totalAusente", stats.getTotalAusente());
            model.addAttribute("totalPermiso", stats.getTotalPermiso());
            model.addAttribute("totalVacaciones", stats.getTotalVacaciones());

            // La lista de los últimos 5 para la tabla de actividad
            model.addAttribute("recientes", asistenciaRepository.findTop5ByFechaOrderByHoraEntradaDesc(hoy));
        }

        return "index";
    }
}

