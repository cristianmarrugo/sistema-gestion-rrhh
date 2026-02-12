package com.example.demo.controller;


import com.example.demo.model.Empleado;
import com.example.demo.service.EmpleadoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/empleados")
@RequiredArgsConstructor
public class EmpleadoViewController {

    private final EmpleadoService empleadoService;

    @GetMapping
    public String listar(Model model, HttpSession session) {

        if (session.getAttribute("empleado") == null) {
            return "redirect:/pin";
        }

        model.addAttribute("empleados", empleadoService.listarTodos());
        model.addAttribute("nuevoEmpleado", new Empleado());

        return "empleados";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Empleado empleado) {
        empleadoService.crear(empleado);
        return "redirect:/empleados";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {

        Empleado empleado = empleadoService.obtenerPorId(id)
                .orElseThrow();

        model.addAttribute("nuevoEmpleado", empleado);
        model.addAttribute("empleados", empleadoService.listarTodos());

        return "empleados";
    }

    @GetMapping("/desactivar/{id}")
    public String desactivar(@PathVariable Long id) {
        empleadoService.desactivar(id);
        return "redirect:/empleados";
    }
}

