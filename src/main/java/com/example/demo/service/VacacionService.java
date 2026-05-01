package com.example.demo.service;

import com.example.demo.model.Empleado;
import com.example.demo.model.EstadoSolicitud;
import com.example.demo.model.Permiso;
import com.example.demo.model.Vacacion;
import com.example.demo.repository.VacacionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VacacionService {

    private final VacacionRepository vacacionRepository;

    /**
     * Verificar si tiene vacaciones activas hoy
     */
    public boolean tieneVacacionesHoy(Empleado empleado) {
        return vacacionRepository.existsVacacionActiva(
                empleado, LocalDate.now()
        );
    }

    /**
     * Calcular días disponibles
     * En Colombia son 15 días hábiles por año trabajado
     */
    public int diasDisponibles(Empleado empleado) {
        // Días anuales según legislación colombiana
        int diasAnuales = 15;

        // Calcular días ya usados (vacaciones aprobadas)
        int diasUsados = vacacionRepository.findAll()
                .stream()
                .filter(v -> v.getEmpleado().getId().equals(empleado.getId()))
                .filter(v -> v.getEstado() == EstadoSolicitud.APROBADO)
                .filter(v -> v.getFechaInicio().getYear() == LocalDate.now().getYear())
                .mapToInt(Vacacion::getDiasSolicitados)
                .sum();

        return Math.max(0, diasAnuales - diasUsados);
    }

    /**
     * Solicitar vacaciones
     */
    @Transactional
    public Vacacion solicitarVacaciones(Empleado empleado, Vacacion vacacion) {
        // Validar que no tenga otras vacaciones en las mismas fechas
        boolean tieneVacacionEnRango = vacacionRepository.findAll()
                .stream()
                .filter(v -> v.getEmpleado().getId().equals(empleado.getId()))
                .filter(v -> v.getEstado() == EstadoSolicitud.APROBADO ||
                        v.getEstado() == EstadoSolicitud.PENDIENTE)
                .anyMatch(v ->
                        // Verificar si hay solapamiento de fechas
                        !(vacacion.getFechaFin().isBefore(v.getFechaInicio()) ||
                                vacacion.getFechaInicio().isAfter(v.getFechaFin()))
                );

        if (tieneVacacionEnRango) {
            throw new RuntimeException(
                    "Ya tienes vacaciones solicitadas o aprobadas en estas fechas"
            );
        }

        // Validar días disponibles
        int disponibles = diasDisponibles(empleado);
        if (vacacion.getDiasSolicitados() > disponibles) {
            throw new RuntimeException(
                    "Solo tienes " + disponibles + " días disponibles. " +
                            "Estás solicitando " + vacacion.getDiasSolicitados() + " días."
            );
        }

        // Crear nueva solicitud
        Vacacion nuevaVacacion = new Vacacion();
        nuevaVacacion.setEmpleado(empleado);
        nuevaVacacion.setFechaInicio(vacacion.getFechaInicio());
        nuevaVacacion.setFechaFin(vacacion.getFechaFin());
        nuevaVacacion.setDiasSolicitados(vacacion.getDiasSolicitados());
        nuevaVacacion.setEstado(EstadoSolicitud.PENDIENTE);

        return vacacionRepository.save(nuevaVacacion);
    }

    /**
     * Listar vacaciones de un empleado
     */
    public List<Vacacion> listarPorEmpleado(Empleado empleado) {
        return vacacionRepository.findAll()
                .stream()
                .filter(v -> v.getEmpleado().getId().equals(empleado.getId()))
                .sorted((a, b) -> b.getId().compareTo(a.getId())) // Más recientes primero
                .toList();
    }

    /**
     * Listar todas las vacaciones
     */
    public List<Vacacion> listarTodas() {
        return vacacionRepository.findAll()
                .stream()
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .toList();
    }

    /**
     * Listar vacaciones pendientes
     */
    public List<Vacacion> listarPendientes() {
        return vacacionRepository.findAll()
                .stream()
                .filter(v -> v.getEstado() == EstadoSolicitud.PENDIENTE)
                .sorted((a, b) -> a.getFechaInicio().compareTo(b.getFechaInicio()))
                .toList();
    }

    /**
     * Aprobar vacaciones
     */
    @Transactional
    public Vacacion aprobar(Long id, Empleado administradorLogueado) {
        Vacacion vacacion = vacacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vacación no encontrada"));

        if (vacacion.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new RuntimeException(
                    "Estas vacaciones ya fueron " + vacacion.getEstado().toString().toLowerCase()
            );
        }
        if (vacacion.getEmpleado().getId().equals(administradorLogueado.getId())) {
            throw new RuntimeException("Acceso denegado: Un usuario de RRHH no puede aprobar sus propias vacaciones.");
        }

        vacacion.setEstado(EstadoSolicitud.APROBADO);
        return vacacionRepository.save(vacacion);
    }

    /**
     * Rechazar vacaciones
     */
    @Transactional
    public Vacacion rechazar(Long id, Empleado administradorLogueado) {
        Vacacion vacacion = vacacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vacación no encontrada"));

        if (vacacion.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new RuntimeException("Esta solicitud ya no está pendiente.");
        }

        // VALIDACIÓN DE SEGURIDAD
        if (vacacion.getEmpleado().getId().equals(administradorLogueado.getId())) {
            throw new RuntimeException("Acceso denegado: No puedes rechazar tus propias vacaciones.");
        }

        vacacion.setEstado(EstadoSolicitud.RECHAZADO);
        return vacacionRepository.save(vacacion);
    }

    /**
     * Obtener vacación por ID
     */
    public Optional<Vacacion> obtenerPorId(Long id) {
        return vacacionRepository.findById(id);
    }

    /**
     * Contar vacaciones pendientes
     */
    public long contarPendientes() {
        return vacacionRepository.findAll()
                .stream()
                .filter(v -> v.getEstado() == EstadoSolicitud.PENDIENTE)
                .count();
    }

    /**
     * Contar vacaciones de un empleado por estado
     */
    public long contarPorEmpleadoYEstado(Empleado empleado, EstadoSolicitud estado) {
        return vacacionRepository.findAll()
                .stream()
                .filter(v -> v.getEmpleado().getId().equals(empleado.getId()))
                .filter(v -> v.getEstado() == estado)
                .count();
    }

    /**
     * Calcular días de vacaciones usados en el año actual
     */
    public int diasUsadosEnElAnio(Empleado empleado) {
        return vacacionRepository.findAll()
                .stream()
                .filter(v -> v.getEmpleado().getId().equals(empleado.getId()))
                .filter(v -> v.getEstado() == EstadoSolicitud.APROBADO)
                .filter(v -> v.getFechaInicio().getYear() == LocalDate.now().getYear())
                .mapToInt(Vacacion::getDiasSolicitados)
                .sum();
    }

    @Transactional
    public void autorizar(Long id, String nombreAdmin) {
        Vacacion vacacion = vacacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vacación no encontrada"));

        vacacion.setEstado(EstadoSolicitud.APROBADO);
        vacacion.setAprobadoPor(nombreAdmin);
        vacacionRepository.save(vacacion);
    }

    @PersistenceContext
    private EntityManager entityManager;

    public List<Vacacion> obtenerHistorial(Long id) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);

        // Consultamos las revisiones específicamente para la clase Vacacion
        return auditReader.createQuery()
                .forRevisionsOfEntity(Vacacion.class, true, true)
                .add(AuditEntity.id().eq(id))
                .getResultList();
    }
}


