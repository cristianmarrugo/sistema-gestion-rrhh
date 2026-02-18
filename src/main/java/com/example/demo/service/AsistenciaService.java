package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class AsistenciaService {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private HoraExtraRepository horaExtraRepository;

    @Autowired
    private PermisoRepository permisoRepository;

    @Autowired
    private VacacionRepository vacacionRepository;

    // 🔁 JOB AUTOMÁTICO DE AUSENCIAS
    @Scheduled(cron = "0 0 23 * * ?")
    public void marcarAusenciasAutomaticas() {

        LocalDate hoy = LocalDate.now();
        List<Empleado> empleados = empleadoRepository.findAll();

        for (Empleado empleado : empleados) {

            if (!empleado.isActivo()) continue;

            boolean yaMarco = asistenciaRepository
                    .findByEmpleadoAndFecha(empleado, hoy)
                    .isPresent();

            boolean tienePermiso =
                    permisoRepository.existsPermisoActivo(empleado, hoy);

            boolean tieneVacaciones =
                    vacacionRepository.existsVacacionActiva(empleado, hoy);

            if (!yaMarco && !tienePermiso && !tieneVacaciones) {
                Asistencia asistencia = new Asistencia();
                asistencia.setEmpleado(empleado);
                asistencia.setFecha(hoy);
                asistencia.setEstado(EstadoAsistencia.AUSENTE);
                asistenciaRepository.save(asistencia);
            }
        }
    }

    public boolean llegoTardeHoy(Empleado empleado) {

        return asistenciaRepository
                .findByEmpleadoAndFecha(empleado, LocalDate.now())
                .map(a -> a.getEstado() == EstadoAsistencia.TARDE)
                .orElse(false);
    }

    public double horasExtraMesActual(Empleado empleado) {
        return horaExtraRepository.horasExtraMesActual(empleado);
    }


    // 🕒 MARCAR ENTRADA / SALIDA
    public Asistencia marcarAsistencia(String pin) {

        Empleado empleado = empleadoRepository.findByPin(pin)
                .orElseThrow(() -> new RuntimeException("PIN inválido"));

        if (!empleado.isActivo()) {
            throw new RuntimeException("Empleado inactivo");
        }

        // VALIDAR QUE TENGA HORARIO ASIGNADO
        if (empleado.getHorario() == null) {
            throw new RuntimeException("Este empleado no tiene un horario asignado. Contacta a RRHH.");
        }

        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        Optional<Asistencia> asistenciaOpt =
                asistenciaRepository.findByEmpleadoAndFecha(empleado, hoy);

        // 👉 SALIDA
        if (asistenciaOpt.isPresent()) {
            Asistencia asistencia = asistenciaOpt.get();
            asistencia.setHoraSalida(ahora);

            calcularHorasExtras(asistencia);

            return asistenciaRepository.save(asistencia);
        }

        // 👉 ENTRADA
        Asistencia asistencia = new Asistencia();
        asistencia.setEmpleado(empleado);
        asistencia.setFecha(hoy);
        asistencia.setHoraEntrada(ahora);

        LocalTime horaHorario = empleado.getHorario().getHoraEntrada();
        int tolerancia = empleado.getHorario().getToleranciaMinutos();

        if (ahora.isAfter(horaHorario.plusMinutes(tolerancia))) {
            asistencia.setEstado(EstadoAsistencia.TARDE);
        } else {
            asistencia.setEstado(EstadoAsistencia.NORMAL);
        }

        return asistenciaRepository.save(asistencia);
    }

    // ⏱️ CÁLCULO DE HORAS EXTRAS
    private void calcularHorasExtras(Asistencia asistencia) {

        if (asistencia.getHoraSalida() == null) return;

        // Validar que el empleado tenga horario
        if (asistencia.getEmpleado().getHorario() == null) return;

        Horario horario = asistencia.getEmpleado().getHorario();

        LocalTime salidaHorario = horario.getHoraSalida();
        LocalTime salidaReal = asistencia.getHoraSalida();

        if (salidaReal.isAfter(salidaHorario)) {

            double horasExtras =
                    Duration.between(salidaHorario, salidaReal)
                            .toMinutes() / 60.0;

            HoraExtra extra = new HoraExtra();
            extra.setEmpleado(asistencia.getEmpleado());
            extra.setFecha(asistencia.getFecha());
            extra.setHoras(horasExtras);
            extra.setTipo(TipoHoraExtra.DIURNA);

            double valorHora =
                    asistencia.getEmpleado()
                            .getCargo()
                            .getSalarioBase() / 240;

            extra.setValor(valorHora * horasExtras * 1.25);

            horaExtraRepository.save(extra);
        }
    }

    // ==================== MÉTODOS DE CONSULTA ====================

    /**
     * Obtener asistencia de hoy del empleado
     */
    public Optional<Asistencia> obtenerAsistenciaHoy(Empleado empleado) {
        return asistenciaRepository.findByEmpleadoAndFecha(empleado, LocalDate.now());
    }

    /**
     * Obtener asistencia por ID
     */
    public Optional<Asistencia> obtenerPorId(Long id) {
        return asistenciaRepository.findById(id);
    }

    /**
     * Listar todas las asistencias
     */
    public List<Asistencia> listarTodas() {
        return asistenciaRepository.findAll();
    }

    /**
     * Listar asistencias por fecha
     */
    public List<Asistencia> listarPorFecha(LocalDate fecha) {
        return asistenciaRepository.findAll()
                .stream()
                .filter(a -> a.getFecha().equals(fecha))
                .toList();
    }

    /**
     * Listar asistencias por empleado
     */
    public List<Asistencia> listarPorEmpleado(Long empleadoId) {
        Empleado empleado = empleadoRepository.findById(empleadoId).orElse(null);
        if (empleado == null) return List.of();

        return asistenciaRepository.findAll()
                .stream()
                .filter(a -> a.getEmpleado().getId().equals(empleadoId))
                .toList();
    }

    /**
     * Listar asistencias por fecha y empleado
     */
    public List<Asistencia> listarPorFechaYEmpleado(LocalDate fecha, Long empleadoId) {
        Empleado empleado = empleadoRepository.findById(empleadoId).orElse(null);
        if (empleado == null) return List.of();

        return asistenciaRepository.findByEmpleadoAndFecha(empleado, fecha)
                .map(List::of)
                .orElse(List.of());
    }

    /**
     * Listar asistencias por empleado y mes
     */
    public List<Asistencia> listarPorEmpleadoYMes(Long empleadoId, int mes, int anio) {
        Empleado empleado = empleadoRepository.findById(empleadoId).orElse(null);
        if (empleado == null) return List.of();

        return asistenciaRepository.findAll()
                .stream()
                .filter(a -> a.getEmpleado().getId().equals(empleadoId))
                .filter(a -> a.getFecha().getMonthValue() == mes)
                .filter(a -> a.getFecha().getYear() == anio)
                .toList();
    }
}


