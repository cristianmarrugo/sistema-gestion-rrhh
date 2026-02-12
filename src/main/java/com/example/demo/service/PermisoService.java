package com.example.demo.service;

import com.example.demo.model.Empleado;
import com.example.demo.model.EstadoSolicitud;
import com.example.demo.model.Permiso;
import com.example.demo.repository.PermisoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PermisoService {

    private final PermisoRepository permisoRepository;

    public boolean tienePermisoHoy(Empleado empleado) {
        return permisoRepository.existsPermisoActivo(
                empleado, LocalDate.now()
        );
    }

    public List<Permiso> listarTodos() {
        return permisoRepository.findAll();
    }

    public List<Permiso> listarPendientes() {
        return permisoRepository.findAll()
                .stream()
                .filter(p -> p.getEstado() == EstadoSolicitud.PENDIENTE)
                .toList();
    }

    public List<Permiso> listarPorEmpleado(Long empleadoId) {
        return permisoRepository.findAll()
                .stream()
                .filter(p -> p.getEmpleado().getId().equals(empleadoId))
                .toList();
    }

    @Transactional
    public Permiso solicitar(Permiso permiso, Empleado empleado) {
        permiso.setEmpleado(empleado);
        permiso.setEstado(EstadoSolicitud.PENDIENTE);
        return permisoRepository.save(permiso);
    }

    @Transactional
    public Optional<Permiso> aprobar(Long id) {
        return permisoRepository.findById(id)
                .map(permiso -> {
                    permiso.setEstado(EstadoSolicitud.APROBADO);
                    return permisoRepository.save(permiso);
                });
    }

    @Transactional
    public Optional<Permiso> rechazar(Long id) {
        return permisoRepository.findById(id)
                .map(permiso -> {
                    permiso.setEstado(EstadoSolicitud.RECHAZADO);
                    return permisoRepository.save(permiso);
                });
    }

    @Transactional
    public boolean eliminar(Long id) {
        return permisoRepository.findById(id)
                .map(permiso -> {
                    permisoRepository.delete(permiso);
                    return true;
                })
                .orElse(false);
    }
}


