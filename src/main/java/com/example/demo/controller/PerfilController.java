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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

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

        return "perfil-empleado";
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
                                  @RequestParam(required = false) String telefono,
                                  @RequestParam(required = false) LocalDate fechaNacimiento,
                                  @RequestParam(required = false) String direccion,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {

        Empleado empleadoSesion = (Empleado) session.getAttribute("empleado");

        // Seguridad: Verificar que el ID que se intenta editar es el del usuario logueado
        if (empleadoSesion == null || !empleadoSesion.getId().equals(id)) {
            return "redirect:/login";
        }

        try {
            // Buscamos el empleado real de la BD
            Empleado empleadoBD = empleadoService.obtenerPorId(id)
                    .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

            // Actualizamos SOLO los campos permitidos para el autoservicio
            empleadoBD.setEmail(email);
            empleadoBD.setTelefono(telefono);
            empleadoBD.setDireccion(direccion);
            empleadoBD.setFechaNacimiento(fechaNacimiento);

            // El método actualizar ya tiene las validaciones de Duplicados (Email/PIN)
            empleadoService.actualizar(id, empleadoBD);

            // Actualizar la sesión para que el nombre/email cambie en el header de inmediato
            session.setAttribute("empleado", empleadoBD);

            empleadoService.actualizar(id, empleadoBD);

            // Volvemos a buscarlo de la BD para estar 100% seguros de que traemos lo que se guardó
            Empleado datosFrescos = empleadoService.obtenerPorId(id).get();
            session.setAttribute("empleado", datosFrescos);

            redirectAttributes.addFlashAttribute("mensaje", "¡Perfil actualizado con éxito!");
            return "redirect:/mi-perfil?success=true";

        } catch (RuntimeException e) {
            // Si el email está repetido, regresamos al formulario con el error
            model.addAttribute("error", e.getMessage());
            model.addAttribute("empleado", empleadoSesion);
            return "editar-perfil";
        }
    }
}
