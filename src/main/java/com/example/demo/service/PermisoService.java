package com.example.demo.service;

import com.example.demo.model.Empleado;
import com.example.demo.model.EstadoSolicitud;
import com.example.demo.model.Permiso;
import com.example.demo.model.Rol;
import com.example.demo.repository.EmpleadoRepository;
import com.example.demo.repository.PermisoRepository;
import com.example.demo.repository.VacacionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PermisoService {

    @Autowired
    private final PermisoRepository permisoRepository;

    @Autowired
    private final NotificacionService notificacionService;

    @Autowired
    private final EmpleadoRepository empleadoRepository;

    @Autowired
    private final VacacionRepository vacacionRepository;

    public boolean tienePermisoHoy(Empleado empleado) {
        return permisoRepository.existsPermisoActivo(
                empleado, LocalDate.now()
        );
    }

    @Transactional
    public void autorizarPermiso(Long permisoId, String nombreAdmin,Empleado administradorLogueado ) {
        Permiso permiso =  permisoRepository.findById(permisoId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        // VALIDACIÓN CLAVE: El admin no puede ser el mismo que solicita
        if (permiso.getEmpleado().getId().equals(administradorLogueado.getId())) {
            throw new RuntimeException("Seguridad: No puedes aprobar tu propia solicitud de permiso. Debe hacerlo otro empleado de recursos humanos.");
        }

        notificacionService.crear(
                permiso.getEmpleado(),
                "✅ Tu solicitud de permiso ha sido APROBADA por " + administradorLogueado.getNombre(),
                "/permisos"
        );
        // Al cambiar estos valores, Envers creará un nuevo registro en la tabla _AUD
        permiso.setEstado(EstadoSolicitud.APROBADO);
        permiso.setAprobadoPor(nombreAdmin);
        permiso.setAprobadoPor(administradorLogueado.getNombre() + " " + administradorLogueado.getApellido());
        permisoRepository.save(permiso);
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
        LocalDate inicio = permiso.getFechaInicio();
        LocalDate fin = permiso.getFechaFin();

        permiso.setEmpleado(empleado);
        permiso.setEstado(EstadoSolicitud.PENDIENTE);
        List<Empleado> rrhh = empleadoRepository.findByRol(Rol.RRHH);

        if (permiso.getFechaFin().isBefore(permiso.getFechaInicio())) {
            throw new RuntimeException("La fecha de fin no puede ser anterior a la de inicio.");
        }

        // 2. VALIDACIÓN CRÍTICA: Evitar duplicados o cruces
        boolean yaTienePermiso = permisoRepository.existeCruceDeFechas(
                permiso.getEmpleado(),
                permiso.getFechaInicio(),
                permiso.getFechaFin()
        );

        if (yaTienePermiso) {
            throw new RuntimeException("Ya tienes un permiso activo o pendiente para esas fechas.");
        }

        boolean estaDeVacaciones = vacacionRepository.existsVacacionAprobadaEnRango(empleado, inicio, fin);

        if (estaDeVacaciones) {
            throw new RuntimeException("No puedes solicitar permisos mientras estás en periodo de vacaciones.");
        }

        String mensaje = "Nueva solicitud de permiso de: " + permiso.getEmpleado().getNombre();
        String url = "/permisos/pendientes"; // O la ruta que uses

        // Usamos el nuevo método que filtra al autor
        notificacionService.notificarGestionadoresExcepto(permiso.getEmpleado(), mensaje, url);
        return permisoRepository.save(permiso);
    }

    @Transactional
    public void rechazarPermiso(Long permisoId, Empleado administradorLogueado) {
        Permiso permiso = permisoRepository.findById(permisoId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        // VALIDACIÓN DE SEGURIDAD
        if (permiso.getEmpleado().getId().equals(administradorLogueado.getId())) {
            throw new RuntimeException("Seguridad: No puedes rechazar tu propia solicitud. Debe gestionarla otro colega de RRHH.");
        }

        permiso.setEstado(EstadoSolicitud.RECHAZADO);
        // Opcional: puedes guardar quién lo rechazó si tienes el campo
        // permiso.setRechazadoPor(administradorLogueado.getNombre() + " " + administradorLogueado.getApellido());

        notificacionService.crear(
                permiso.getEmpleado(),
                "❌ Tu solicitud de permiso ha sido RECHAZADA.",
                "/permisos"
        );

        permisoRepository.save(permiso);
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

    @PersistenceContext
    private EntityManager entityManager;

    public List<Permiso> obtenerHistorial(Long solicitudId) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);

        // Esto te devuelve todas las versiones que ha tenido esa solicitud
        return auditReader.createQuery()
                .forRevisionsOfEntity(Permiso.class, true, true)
                .add(AuditEntity.id().eq(solicitudId))
                .getResultList();
    }
}


