package com.example.demo.controller;

import com.example.demo.model.Empleado;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/index")
    public String index(HttpSession session, Model model) {
        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return "redirect:/pin";
        }

        model.addAttribute("empleado", empleado);
        return "index";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/pin";
    }

    @GetMapping("/asistencia")
    public String asistencia(HttpSession session, Model model) {
        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return "redirect:/pin";
        }

        model.addAttribute("pin", empleado.getPin());
        return "asistencia";
    }

    @GetMapping("/escanear-qr")
    public String escanearQR(HttpSession session) {
        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return "redirect:/pin";
        }

        // Solo ADMIN y RRHH pueden escanear QR
        if (!"ADMIN".equals(empleado.getRol().toString()) &&
                !"RRHH".equals(empleado.getRol().toString())) {
            return "redirect:/index";
        }

        return "escanear-qr";
    }

    // Vistas pendientes de crear (placeholder)

    @GetMapping("/empleados")
    public String empleados(HttpSession session, Model model) {
        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return "redirect:/pin";
        }

        // Pasar el rol del usuario a la vista
        model.addAttribute("usuarioRol", empleado.getRol().toString());

        return "empleados";
    }

    @GetMapping("/cargos")
    public String cargos(HttpSession session) {
        if (session.getAttribute("empleado") == null) {
            return "redirect:/pin";
        }
        return "cargos";
    }

    @GetMapping("/horarios")
    public String horarios(HttpSession session) {
        if (session.getAttribute("empleado") == null) {
            return "redirect:/pin";
        }
        return "horarios";
    }

    @GetMapping("/permisos")
    public String permisos(HttpSession session) {
        if (session.getAttribute("empleado") == null) {
            return "redirect:/pin";
        }
        return "permisos";
    }

    @GetMapping("/permisos/pendientes")
    public String permisosPendientes(HttpSession session) {
        if (session.getAttribute("empleado") == null) {
            return "redirect:/pin";
        }
        return "permisos-pendientes";
    }

    @GetMapping("/vacaciones")
    public String vacaciones(HttpSession session) {
        if (session.getAttribute("empleado") == null) {
            return "redirect:/pin";
        }
        return "vacaciones";
    }

    @GetMapping("/vacaciones/pendientes")
    public String vacacionesPendientes(HttpSession session) {
        if (session.getAttribute("empleado") == null) {
            return "redirect:/pin";
        }
        return "vacaciones-pendientes";
    }

    @GetMapping("/reportes-asistencia")
    public String reportes(HttpSession session) {
        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return "redirect:/pin";
        }

        // Solo ADMIN y RRHH pueden ver reportes
        if (!"ADMIN".equals(empleado.getRol().toString()) &&
                !"RRHH".equals(empleado.getRol().toString())) {
            return "redirect:/index";
        }

        return "reportes-asistencia";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {
        if (session.getAttribute("empleado") == null) {
            return "redirect:/pin";
        }
        return "dashboard-powerbi";
    }
}


