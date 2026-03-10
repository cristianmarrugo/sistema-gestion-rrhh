package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.AsignacionTurnoRepository;
import com.example.demo.repository.EmpleadoRepository;
import com.example.demo.repository.TurnoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TurnoService {
    private final TurnoRepository turnoRepository;
    private final AsignacionTurnoRepository asignacionTurnoRepository;
    private final EmpleadoRepository empleadoRepository;

        /**
         * Asignar un patrón cíclico a un empleado para un mes completo
         * Ejemplo: [M, M, M, M, M, L, L] (5 días mañana, 2 libres)
         */

        @Transactional
        public void asignarPatronCiclico(Long empleadoId,
                                         List<Long> patronTurnoIds,
                                         int mes,
                                         int anio) {

            Empleado empleado = empleadoRepository.findById(empleadoId)
                    .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

            YearMonth yearMonth = YearMonth.of(anio, mes);
            LocalDate inicio = yearMonth.atDay(1);
            LocalDate fin = yearMonth.atEndOfMonth();

            int patronIndex = 0;
            LocalDate fecha = inicio;

            while (!fecha.isAfter(fin)) {
                Long turnoId = patronTurnoIds.get(patronIndex % patronTurnoIds.size());

                AsignacionTurno asignacion = new AsignacionTurno();
                asignacion.setEmpleado(empleado);
                asignacion.setFecha(fecha);

                if (turnoId != null && turnoId > 0) {
                    Turno turno = turnoRepository.findById(turnoId)
                            .orElseThrow(() -> new RuntimeException("Turno no encontrado"));
                    asignacion.setTurno(turno);
                    asignacion.setTipo(TipoAsignacion.NORMAL);
                } else {
                    asignacion.setTurno(null);
                    asignacion.setTipo(TipoAsignacion.LIBRE);
                }

                asignacionTurnoRepository.save(asignacion);

                fecha = fecha.plusDays(1);
                patronIndex++;
            }
        }

        /**
         * Asignar un turno fijo a un empleado para todo un mes
         * (Mismo turno todos los días laborables)
         */
        @Transactional
        public void asignarTurnoFijoMensual(Long empleadoId,
                                            Long turnoId,
                                            int mes,
                                            int anio,
                                            boolean incluirFinDeSemana) {

            Empleado empleado = empleadoRepository.findById(empleadoId)
                    .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

            Turno turno = turnoRepository.findById(turnoId)
                    .orElseThrow(() -> new RuntimeException("Turno no encontrado"));

            YearMonth yearMonth = YearMonth.of(anio, mes);
            LocalDate inicio = yearMonth.atDay(1);
            LocalDate fin = yearMonth.atEndOfMonth();
            LocalDate fecha = inicio;

            while (!fecha.isAfter(fin)) {
                DayOfWeek diaSemana = fecha.getDayOfWeek();

                // Saltar fines de semana si no están incluidos
                if (!incluirFinDeSemana &&
                        (diaSemana == DayOfWeek.SATURDAY || diaSemana == DayOfWeek.SUNDAY)) {
                    fecha = fecha.plusDays(1);
                    continue;
                }

                AsignacionTurno asignacion = new AsignacionTurno();
                asignacion.setEmpleado(empleado);
                asignacion.setTurno(turno);
                asignacion.setFecha(fecha);
                asignacion.setTipo(TipoAsignacion.NORMAL);

                asignacionTurnoRepository.save(asignacion);

                fecha = fecha.plusDays(1);
            }
        }

        /**
         * Asignar un turno específico para un día concreto (manual)
         */
        @Transactional
        public AsignacionTurno asignarTurnoDia(Long empleadoId,
                                               Long turnoId,
                                               LocalDate fecha) {

            Empleado empleado = empleadoRepository.findById(empleadoId)
                    .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

            // Verificar si ya existe asignación para ese día
            asignacionTurnoRepository.findByEmpleadoAndFecha(empleado, fecha)
                    .ifPresent(asignacionTurnoRepository::delete);

            AsignacionTurno asignacion = new AsignacionTurno();
            asignacion.setEmpleado(empleado);
            asignacion.setFecha(fecha);

            if (turnoId != null && turnoId > 0) {
                Turno turno = turnoRepository.findById(turnoId)
                        .orElseThrow(() -> new RuntimeException("Turno no encontrado"));
                asignacion.setTurno(turno);
                asignacion.setTipo(TipoAsignacion.NORMAL);
            } else {
                asignacion.setTurno(null);
                asignacion.setTipo(TipoAsignacion.LIBRE);
            }

            return asignacionTurnoRepository.save(asignacion);
        }

        /**
         * Obtener el turno de un empleado en una fecha específica
         */
        public Turno obtenerTurnoEmpleado(Long empleadoId, LocalDate fecha) {
            Empleado empleado = empleadoRepository.findById(empleadoId)
                    .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

            return asignacionTurnoRepository.findByEmpleadoAndFecha(empleado, fecha)
                    .map(AsignacionTurno::getTurno)
                    .orElse(null);
        }

        /**
         * Obtener cuadrante mensual de un empleado
         */
        public List<AsignacionTurno> obtenerCuadranteMensual(Long empleadoId,
                                                             int mes,
                                                             int anio) {
            Empleado empleado = empleadoRepository.findById(empleadoId)
                    .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

            YearMonth yearMonth = YearMonth.of(anio, mes);
            LocalDate inicio = yearMonth.atDay(1);
            LocalDate fin = yearMonth.atEndOfMonth();

            return asignacionTurnoRepository.findByEmpleadoAndFechaBetween(
                    empleado, inicio, fin
            );
        }

        /**
         * Limpiar asignaciones de un mes para rehacer el cuadrante
         */
        @Transactional
        public void limpiarCuadranteMensual(Long empleadoId, int mes, int anio) {
            Empleado empleado = empleadoRepository.findById(empleadoId)
                    .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

            YearMonth yearMonth = YearMonth.of(anio, mes);
            LocalDate inicio = yearMonth.atDay(1);
            LocalDate fin = yearMonth.atEndOfMonth();

            List<AsignacionTurno> asignaciones =
                    asignacionTurnoRepository.findByEmpleadoAndFechaBetween(
                            empleado, inicio, fin
                    );

            asignacionTurnoRepository.deleteAll(asignaciones);
        }
    }

