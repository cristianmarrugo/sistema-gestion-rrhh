package com.example.demo.service;

import com.example.demo.model.Empleado;
import com.example.demo.model.Notificacion;
import com.example.demo.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    @Autowired
    private final NotificacionRepository repository;

    public void crear(Empleado destinatario, String mensaje, String url) {
        Notificacion n = new Notificacion();

        n.setDestinatario(destinatario);

        n.setMensaje(mensaje);
        n.setUrl(url);
        n.setFechaCreacion(LocalDateTime.now());
        repository.save(n);
    }

    public List<Notificacion> listarNoLeidas(Empleado destinatario) {
        return repository.findByDestinatarioAndLeidaFalseOrderByFechaCreacionDesc(destinatario);
    }
}
