package com.example.demo.controller;

import com.example.demo.model.Empleado;
import com.example.demo.service.EmpleadoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PerfilController {

    private final EmpleadoService empleadoService;

    @GetMapping("/mi-perfil")
    public String verMiPerfil(HttpSession session, Model model) {
        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return "redirect:/login";
        }

        // Recargar desde BD para tener datos actualizados
        empleado = empleadoService.obtenerPorId(empleado.getId()).orElse(empleado);
        model.addAttribute("empleado", empleado);

        return "mi-perfil";
    }

    @GetMapping("/mi-perfil/editar")
    public String editarMiPerfil(HttpSession session, Model model) {
        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return "redirect:/login";
        }

        empleado = empleadoService.obtenerPorId(empleado.getId()).orElse(empleado);
        model.addAttribute("empleado", empleado);

        return "editar-perfil";
    }

    @PostMapping("/mi-perfil/editar")
    public String guardarMiPerfil(@RequestParam Long id,
                                  @RequestParam String email,
                                  @RequestParam String telefono,
                                  @RequestParam(required = false) String direccion,
                                  HttpSession session,
                                  Model model) {

        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null || !empleado.getId().equals(id)) {
            return "redirect:/login";
        }

        // Actualizar solo campos que el empleado puede modificar
        Empleado empleadoActualizado = empleadoService.obtenerPorId(id).orElse(null);

        if (empleadoActualizado == null) {
            return "redirect:/login";
        }

        empleadoActualizado.setEmail(email);
        // Si tienes campos de teléfono y dirección en tu modelo, descoméntalos:
        // empleadoActualizado.setTelefono(telefono);
        // empleadoActualizado.setDireccion(direccion);

        empleadoService.actualizar(id, empleadoActualizado);

        // Actualizar sesión con datos nuevos
        empleadoActualizado = empleadoService.obtenerPorId(id).orElse(empleadoActualizado);
        session.setAttribute("empleado", empleadoActualizado);

        model.addAttribute("mensaje", "Perfil actualizado correctamente");
        model.addAttribute("empleado", empleadoActualizado);

        return "redirect:/mi-perfil?success=true";
    }
}
