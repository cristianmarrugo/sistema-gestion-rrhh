package com.example.demo.controller;

import com.example.demo.model.Empleado;
import com.example.demo.model.Turno;
import com.example.demo.repository.TurnoRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/turnos")
@RequiredArgsConstructor
public class TurnoController {

    private final TurnoRepository turnoRepository;

    /**
     * Listar todos los turnos
     */
    @GetMapping
    public String listarTurnos(HttpSession session, Model model) {
        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return "redirect:/login";
        }

        if (!"ADMIN".equals(empleado.getRol().toString()) &&
                !"RRHH".equals(empleado.getRol().toString())) {
            return "redirect:/?error=forbidden";
        }

        List<Turno> turnos = turnoRepository.findAll();
        model.addAttribute("empleado", empleado);
        model.addAttribute("turnos", turnos);

        return "turnos/listar";
    }

    /**
     * Formulario para crear nuevo turno
     */
    @GetMapping("/nuevo")
    public String formularioNuevo(HttpSession session, Model model) {
        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return "redirect:/login";
        }

        model.addAttribute("empleado", empleado);
        model.addAttribute("turno", new Turno());

        return "turnos/formulario";
    }

    /**
     * Guardar turno nuevo o editado
     */
    @PostMapping("/guardar")
    public String guardarTurno(@ModelAttribute Turno turno,
                               HttpSession session) {
        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return "redirect:/login";
        }

        turnoRepository.save(turno);

        return "redirect:/turnos?success=true";
    }

    /**
     * Formulario para editar turno
     */
    @GetMapping("/editar/{id}")
    public String formularioEditar(@PathVariable Long id,
                                   HttpSession session,
                                   Model model) {
        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return "redirect:/login";
        }

        Turno turno = turnoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Turno no encontrado"));

        model.addAttribute("empleado", empleado);
        model.addAttribute("turno", turno);

        return "turnos/formulario";
    }

    /**
     * Eliminar turno (solo ADMIN)
     */
    @PostMapping("/eliminar/{id}")
    public String eliminarTurno(@PathVariable Long id,
                                HttpSession session) {
        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return "redirect:/login";
        }

        // Solo ADMIN puede eliminar (ya validado en SecurityConfig)
        turnoRepository.deleteById(id);

        return "redirect:/turnos?deleted=true";
    }
}