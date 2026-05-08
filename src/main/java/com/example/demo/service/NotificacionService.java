package com.example.demo.service;

import com.example.demo.model.Empleado;
import com.example.demo.model.Notificacion;
import com.example.demo.model.Rol;
import com.example.demo.repository.EmpleadoRepository;
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

    @Autowired
    private final EmpleadoRepository empleadoRepository;

    public void notificarGestionadoresExcepto(Empleado autor, String mensaje, String url) {
        // Buscamos a todos los que pueden gestionar (ADMIN y RRHH)
        List<Empleado> gestionadores = empleadoRepository.findByRolIn(List.of(Rol.ADMIN, Rol.RRHH));

        for (Empleado gestor : gestionadores) {
            // AQUÍ ESTÁ LA CLAVE: Si el gestor es el mismo que pidió el permiso, lo saltamos
            if (!gestor.getId().equals(autor.getId())) {
                crear(gestor, mensaje, url);
            }
        }
    }

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
