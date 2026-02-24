package com.example.demo.controller;

import com.example.demo.model.Empleado;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardPowerBIController {

    @GetMapping("/dashboard-powerbi")
    public String dashboardPowerBI(HttpSession session) {
        // Verificar autenticación
        Empleado empleado = (Empleado) session.getAttribute("empleado");
        if (empleado == null) {
            return "redirect:/login";
        }

        // Solo ADMIN y RRHH pueden ver el dashboard
        if (!"ADMIN".equals(empleado.getRol().toString()) &&
                !"RRHH".equals(empleado.getRol().toString())) {
            return "redirect:/";
        }

        return "dashboard-powerbi";
    }
}
