package com.example.demo.service;

import com.example.demo.model.AsignacionTurno;
import com.example.demo.model.Asistencia;
import com.example.demo.repository.AsignacionTurnoRepository;
import com.example.demo.repository.AsistenciaRepository;
import lombok.extern.slf4j.Slf4j; // Para ver logs profesionales
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Slf4j // Esto te permite usar log.info o log.error
@Component
public class TareaSalidaAutomatica {

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private AsignacionTurnoRepository asignacionTurnoRepository;

    // Se ejecuta todos los días a las 2:00 AM
    @Scheduled(cron = "0 0 2 * * *")
    public void cerrarTurnosOlvidados() {
        LocalDate ayer = LocalDate.now().minusDays(1);
        log.info("Iniciando cierre automático para la fecha: {}", ayer);

        try {
            List<Asistencia> pendientes = asistenciaRepository.findByFechaAndHoraSalidaIsNull(ayer);
            log.info("Se encontraron {} registros pendientes.", pendientes.size());

            for (Asistencia asis : pendientes) {
                try { // TRY-CATCH INTERNO: Si falla un empleado, sigue con el siguiente
                    AsignacionTurno asignacion = asignacionTurnoRepository
                            .findByEmpleadoIdAndFecha(asis.getEmpleado().getId(), ayer)
                            .orElse(null);

                    if (asignacion != null && asignacion.getTurno() != null) {
                        LocalTime salidaOficial = asignacion.getTurno().getHoraSalida();

                        asis.setHoraSalida(salidaOficial);
                        asis.setObservacion("CIERRE AUTOMÁTICO: El empleado no marcó salida.");

                        asistenciaRepository.save(asis);
                        log.info("Salida cerrada para empleado ID: {}", asis.getEmpleado().getId());
                    } else {
                        log.warn("No se encontró turno asignado para el empleado ID: {} en la fecha {}",
                                asis.getEmpleado().getId(), ayer);
                    }
                } catch (Exception e) {
                    log.error("Error procesando el registro ID: {}. Error: {}", asis.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("ERROR CRÍTICO en la tarea programada: {}", e.getMessage());
        }
        log.info("Proceso de cierre automático finalizado.");
    }
}