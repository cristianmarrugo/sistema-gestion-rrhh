package com.example.demo.controller;


import com.example.demo.model.Empleado;
import com.example.demo.model.Notificacion;
import com.example.demo.service.NotificacionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final NotificacionService notificacionService;

    // Este método  se ejecuta antes de cargar cualquier vista
    @ModelAttribute
    public void agregarNotificacionesAlModelo(HttpSession session, Model model) {
        Empleado usuarioLogueado = (Empleado) session.getAttribute("empleado");

        List<Notificacion> lista = new ArrayList<>();

        if (usuarioLogueado != null) {
            // Si hay usuario, buscamos sus notificaciones no leídas
            lista = notificacionService.listarNoLeidas(usuarioLogueado);
        }

        // Enviamos la lista al modelo (aunque esté vacía, ya no será null)
        model.addAttribute("notificaciones", lista);
    }
}
