package com.example.demo.controller;

import com.example.demo.model.Empleado;
import com.example.demo.model.Notificacion;
import com.example.demo.repository.NotificacionRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequestMapping("/notificaciones")
public class NotificacionController {

    @Autowired
    private NotificacionRepository repository;

    @GetMapping("/leer/{id}")
    public String leer(@PathVariable Long id, HttpSession session) {
        Empleado emp = (Empleado) session.getAttribute("empleado");

        Notificacion n = repository.findById(id).orElseThrow();

        if (!n.getDestinatario().getId().equals(emp.getId())) {
            throw new RuntimeException("Acceso denegado");
        }

        n.setLeida(true);

        repository.save(n);

        return "redirect:" + n.getUrl();
    }
}