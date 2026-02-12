package com.example.demo.service;

import com.example.demo.model.Empleado;
import com.example.demo.model.EstadoSolicitud;
import com.example.demo.model.Vacacion;
import com.example.demo.repository.VacacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VacacionService {

    private final VacacionRepository vacacionRepository;

    public boolean tieneVacacionesHoy(Empleado empleado) {
        return vacacionRepository.existsVacacionActiva(
                empleado, LocalDate.now()
        );
    }

    public List<Vacacion> listarTodas() {
        return vacacionRepository.findAll();
    }

    public List<Vacacion> listarPendientes() {
        return vacacionRepository.findAll()
                .stream()
                .filter(v -> v.getEstado() == EstadoSolicitud.PENDIENTE)
                .toList();
    }

    public List<Vacacion> listarPorEmpleado(Long empleadoId) {
        return vacacionRepository.findAll()
                .stream()
                .filter(v -> v.getEmpleado().getId().equals(empleadoId))
                .toList();
    }

    /**
     * Calcula días disponibles de vacaciones según legislación colombiana:
     * - 15 días hábiles por año trabajado
     * - Se acumulan proporcionalmente
     */
    public int diasDisponibles(Empleado empleado) {
        // Días anuales según ley colombiana
        int diasAnuales = 15;

        // Calcular días ya usados (aprobados)
        int diasUsados = vacacionRepository.findAll()
                .stream()
                .filter(v -> v.getEmpleado().getId().equals(empleado.getId()))
                .filter(v -> v.getEstado() == EstadoSolicitud.APROBADO)
                .filter(v -> v.getFechaInicio().getYear() == LocalDate.now().getYear())
                .mapToInt(Vacacion::getDiasSolicitados)
                .sum();

        return diasAnuales - diasUsados;
    }

    @Transactional
    public Vacacion solicitar(Vacacion vacacion, Empleado empleado) {
        // Calcular días solicitados
        long dias = ChronoUnit.DAYS.between(
                vacacion.getFechaInicio(),
                vacacion.getFechaFin()
        ) + 1;

        vacacion.setDiasSolicitados((int) dias);

        // Validar que tenga días disponibles
        int disponibles = diasDisponibles(empleado);
        if (dias > disponibles) {
            throw new RuntimeException(
                    "No tienes suficientes días disponibles. Tienes: " + disponibles
            );
        }

        vacacion.setEmpleado(empleado);
        vacacion.setEstado(EstadoSolicitud.PENDIENTE);

        return vacacionRepository.save(vacacion);
    }

    @Transactional
    public Optional<Vacacion> aprobar(Long id) {
        return vacacionRepository.findById(id)
                .map(vacacion -> {
                    vacacion.setEstado(EstadoSolicitud.APROBADO);
                    return vacacionRepository.save(vacacion);
                });
    }

    @Transactional
    public Optional<Vacacion> rechazar(Long id) {
        return vacacionRepository.findById(id)
                .map(vacacion -> {
                    vacacion.setEstado(EstadoSolicitud.RECHAZADO);
                    return vacacionRepository.save(vacacion);
                });
    }

    @Transactional
    public boolean eliminar(Long id) {
        return vacacionRepository.findById(id)
                .map(vacacion -> {
                    vacacionRepository.delete(vacacion);
                    return true;
                })
                .orElse(false);
    }
}


