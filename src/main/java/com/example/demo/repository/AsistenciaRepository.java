package com.example.demo.repository;

import com.example.demo.model.Asistencia;
import com.example.demo.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {
    Optional<Asistencia> findByEmpleadoAndFecha(Empleado empleado, LocalDate fecha);

    List<Asistencia> findByEmpleadoAndFechaBetween(Empleado empleado, LocalDate fechaInicio, LocalDate fechaFin);

    List<Asistencia> findByFechaAndHoraSalidaIsNull(LocalDate fecha);

    // Obtiene los últimos 5 registros de hoy, ordenados por hora de entrada descendente
    List<Asistencia> findTop5ByFechaOrderByHoraEntradaDesc(LocalDate fecha);

    List<Asistencia> findByFechaBetweenAndEmpleadoId(LocalDate desde, LocalDate hasta, Long empleadoId);

    List<Asistencia> findByFechaBetween(LocalDate desde, LocalDate hasta);


        // Consulta para sacar el promedio de minutos de retraso de un empleado
        @Query("SELECT AVG(TIMESTAMPDIFF(MINUTE, t.horaEntrada, a.horaEntrada)) " +
                "FROM Asistencia a " +
                "JOIN a.empleado e " +
                "JOIN AsignacionTurno at ON (at.empleado.id = e.id AND at.fecha = a.fecha) " +
                "JOIN at.turno t " +
                "WHERE e.id = :empleadoId AND a.estado = 'TARDE'")
        Double getPromedioMinutosRetraso(Long empleadoId);

}
