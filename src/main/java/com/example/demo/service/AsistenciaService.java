package com.example.demo.service;

import com.example.demo.dto.AsistenciaStats;
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
import java.util.concurrent.atomic.AtomicInteger;

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

    @Autowired
    private TurnoRepository turnoRepository;

    @Autowired
    TurnoService turnoService;

    // 🔁 JOB AUTOMÁTICO DE AUSENCIAS (23:00 cada día)
    @Scheduled(cron = "0 0 23 * * ?")
    public void marcarAusenciasAutomaticas() {

        LocalDate hoy = LocalDate.now();
        List<Empleado> empleados = empleadoRepository.findAll();

        System.out.println("🔄 [JOB AUSENCIAS] Iniciando para " + hoy + " - Total empleados: " + empleados.size());

        int ausenciasMarcadas = 0;
        int yaRegistrados = 0;
        int conPermiso = 0;
        int conVacaciones = 0;
        int sinHorario = 0;

        for (Empleado empleado : empleados) {

            if (!empleado.isActivo()) {
                System.out.println("  ⏭️ Saltando empleado inactivo: " + empleado.getNombre());
                continue;
            }

            // Verificar si ya marcó asistencia
            boolean yaMarco = asistenciaRepository
                    .findByEmpleadoAndFecha(empleado, hoy)
                    .isPresent();

            if (yaMarco) {
                System.out.println("  ✅ " + empleado.getNombre() + " ya tiene asistencia registrada");
                yaRegistrados++;
                continue;
            }

            // Verificar permisos
            boolean tienePermiso = permisoRepository.existsPermisoActivo(empleado, hoy);

            if (tienePermiso) {
                System.out.println("  📋 " + empleado.getNombre() + " tiene permiso aprobado");
                conPermiso++;
                continue;
            }

            // Verificar vacaciones
            boolean tieneVacaciones = vacacionRepository.existsVacacionActiva(empleado, hoy);

            if (tieneVacaciones) {
                System.out.println("  🏖️ " + empleado.getNombre() + " está de vacaciones");
                conVacaciones++;
                continue;
            }

            // Verificar si tiene turno asignado para hoy
            Turno turnoDelDia = turnoService.obtenerTurnoEmpleado(empleado.getId(), hoy);

            // Si no tiene turno ni horario fijo, no marcar ausencia
            if (turnoDelDia == null && empleado.getHorario() == null) {
                System.out.println("  ⚠️ " + empleado.getNombre() + " no tiene turno ni horario asignado");
                sinHorario++;
                continue;
            }

            // ⭐ MARCAR AUSENCIA
            Asistencia asistencia = new Asistencia();
            asistencia.setEmpleado(empleado);
            asistencia.setFecha(hoy);
            asistencia.setEstado(EstadoAsistencia.AUSENTE);
            asistenciaRepository.save(asistencia);

            System.out.println("  ❌ AUSENCIA marcada para: " + empleado.getNombre());
            ausenciasMarcadas++;
        }

        System.out.println("✅ [JOB AUSENCIAS] Completado:");
        System.out.println("   - Ausencias marcadas: " + ausenciasMarcadas);
        System.out.println("   - Ya registrados: " + yaRegistrados);
        System.out.println("   - Con permiso: " + conPermiso);
        System.out.println("   - Con vacaciones: " + conVacaciones);
        System.out.println("   - Sin horario: " + sinHorario);
    }

    // 🔁 JOB AUTOMÁTICO PARA MARCAR SALIDAS OLVIDADAS (23:30 cada día)
    // 🔁 JOB AUTOMÁTICO PARA MARCAR SALIDAS OLVIDADAS (23:30 cada día)
    @Scheduled(cron = "0 30 23 * * ?")
    public void marcarSalidasOlvidadas() {
        LocalDate hoy = LocalDate.now();
        // 💡 Mejora Senior: Solo traemos empleados activos para ahorrar memoria
        List<Empleado> empleadosActivos = empleadoRepository.findAll().stream()
                .filter(Empleado::isActivo)
                .toList();

        System.out.println("🔄 [JOB SALIDAS] Procesando salidas pendientes para: " + hoy);
        AtomicInteger salidasMarcadas = new AtomicInteger(0);

        for (Empleado empleado : empleadosActivos) {
            // Buscamos la asistencia de hoy que tenga entrada pero NO salida
            asistenciaRepository.findByEmpleadoAndFecha(empleado, hoy).ifPresent(asistencia -> {
                if (asistencia.getHoraEntrada() != null && asistencia.getHoraSalida() == null) {

                    // 💡 Lógica de Negocio: Obtener la hora teórica de salida
                    Turno turnoDelDia = turnoService.obtenerTurnoEmpleado(empleado.getId(), hoy);
                    LocalTime horaSalidaTeorica = null;

                    if (turnoDelDia != null) {
                        horaSalidaTeorica = turnoDelDia.getHoraSalida();
                    } else if (empleado.getHorario() != null) {
                        horaSalidaTeorica = empleado.getHorario().getHoraSalida();
                    }

                    if (horaSalidaTeorica != null) {
                        asistencia.setHoraSalida(horaSalidaTeorica);
                        // 💡 IMPORTANTE: No llamamos a calcularHorasExtras() aquí.
                        // Como castigo/incentivo, solo le reconocemos su horario base.
                        asistenciaRepository.save(asistencia);
                        salidasMarcadas.incrementAndGet();
                        System.out.println("  ⚠️ Salida forzada: " + empleado.getNombre() + " -> " + horaSalidaTeorica);
                    }
                }
            });
        }
        System.out.println("✅ [JOB SALIDAS] Finalizado. Total: " + salidasMarcadas);
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

        // SISTEMA HÍBRIDO: Intentar obtener turno, si no existe usar horario antiguo
        Turno turnoDelDia = turnoService.obtenerTurnoEmpleado(empleado.getId(), hoy);

        LocalTime horaHorario;
        int tolerancia;

        if (turnoDelDia != null) {
            // Usar turno asignado (nuevo sistema)
            horaHorario = turnoDelDia.getHoraEntrada();
            tolerancia = turnoDelDia.getToleranciaMinutos();
        } else if (empleado.getHorario() != null) {
            // Fallback al horario fijo (sistema antiguo)
            horaHorario = empleado.getHorario().getHoraEntrada();
            tolerancia = empleado.getHorario().getToleranciaMinutos();
        } else {
            throw new RuntimeException("No tienes horario ni turno asignado para hoy. Contacta a RRHH.");
        }

        // Validar tardanza
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

        // SISTEMA HÍBRIDO: Intentar obtener turno, si no existe usar horario antiguo
        Turno turnoDelDia = turnoService.obtenerTurnoEmpleado(
                asistencia.getEmpleado().getId(),
                asistencia.getFecha()
        );

        LocalTime salidaHorario;

        if (turnoDelDia != null) {
            // Usar turno asignado
            salidaHorario = turnoDelDia.getHoraSalida();
        } else if (asistencia.getEmpleado().getHorario() != null) {
            // Fallback al horario fijo
            salidaHorario = asistencia.getEmpleado().getHorario().getHoraSalida();
        } else {
            return; // No tiene horario definido
        }

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

    public List<Asistencia> obtenerPorEmpleadoYRangoFechas(Long empleadoId, LocalDate desde, LocalDate hasta) {
        Empleado empleado = empleadoRepository.findById(empleadoId).orElse(null);
        if (empleado == null) return List.of();

        return asistenciaRepository.findAll()
                .stream()
                .filter(a -> a.getEmpleado().getId().equals(empleadoId))
                .filter(a -> !a.getFecha().isBefore(desde) && !a.getFecha().isAfter(hasta))
                .sorted((a, b) -> a.getFecha().compareTo(b.getFecha()))
                .toList();
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

    public List<Asistencia> listarPorMes(int mes, int anio) {
        return asistenciaRepository.findAll()
                .stream()
                .filter(a -> a.getFecha().getMonthValue() == mes &&
                        a.getFecha().getYear() == anio)
                .sorted((a, b) -> {
                    // Ordenar por fecha primero, luego por empleado
                    int fechaComp = a.getFecha().compareTo(b.getFecha());
                    if (fechaComp != 0) return fechaComp;
                    return a.getEmpleado().getNombre().compareTo(b.getEmpleado().getNombre());
                })
                .toList();
    }

    public AsistenciaStats obtenerEstadisticas(LocalDate desde, LocalDate hasta, Long empleadoId, String estadoStr) {
        List<Asistencia> registros;

        if (empleadoId != null) {
            registros = asistenciaRepository.findByFechaBetweenAndEmpleadoId(desde, hasta, empleadoId);
        } else {
            registros = asistenciaRepository.findByFechaBetween(desde, hasta);
        }

        // COMPARACIÓN CORRECTA USANDO EL ENUM
        long normal = registros.stream().filter(r -> r.getEstado() == EstadoAsistencia.NORMAL).count();
        long tarde = registros.stream().filter(r -> r.getEstado() == EstadoAsistencia.TARDE).count();
        long ausente = registros.stream().filter(r -> r.getEstado() == EstadoAsistencia.AUSENTE).count();
        long permiso = registros.stream().filter(r -> r.getEstado() == EstadoAsistencia.PERMISO).count();
        long vacaciones = registros.stream().filter(r -> r.getEstado() == EstadoAsistencia.VACACIONES).count();

        return new AsistenciaStats(normal, tarde, ausente, permiso, vacaciones);
    }

    /**
     * Listar asistencias por rango de fechas
     */
    public List<Asistencia> listarPorRango(LocalDate desde, LocalDate hasta) {
        return asistenciaRepository.findAll()
                .stream()
                .filter(a -> !a.getFecha().isBefore(desde) && !a.getFecha().isAfter(hasta))
                .sorted((a, b) -> {
                    int fechaComp = a.getFecha().compareTo(b.getFecha());
                    if (fechaComp != 0) return fechaComp;
                    return a.getEmpleado().getNombre().compareTo(b.getEmpleado().getNombre());
                })
                .toList();
    }

    // Nuevo método para calcular horas totales trabajadas (no solo extras)
    public double calcularHorasTotales(LocalTime entrada, LocalTime salida) {
        if (entrada == null || salida == null) return 0.0;

        Duration duracion = Duration.between(entrada, salida);
        // Convertimos minutos a decimal (ej: 30 min -> 0.5)
        return duracion.toMinutes() / 60.0;
    }
}


