package com.example.demo.service;

import com.example.demo.model.Empleado;
import com.example.demo.model.HistorialCambio;
import com.example.demo.repository.HistorialCambioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaService {
    @Autowired
    private HistorialCambioRepository repo;

    public void registrar(String entidad, Long id, String accion, String detalle, Empleado autor) {
        HistorialCambio log = new HistorialCambio();
        log.setEntidad(entidad);
        log.setEntidadId(id);
        log.setAccion(accion);
        log.setDetalle(detalle);
        log.setRealizadoPor(autor);
        repo.save(log);
    }
}
