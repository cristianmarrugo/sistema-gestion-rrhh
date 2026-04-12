package com.example.demo.repository;

import com.example.demo.model.AsignacionTurno;
import com.example.demo.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AsignacionTurnoRepository extends JpaRepository<AsignacionTurno, Long> {

    Optional<AsignacionTurno> findByEmpleadoAndFecha(Empleado empleado, LocalDate fecha);

    List<AsignacionTurno> findByEmpleadoAndFechaBetween(
            Empleado empleado,
            LocalDate inicio,
            LocalDate fin
    );

    List<AsignacionTurno> findByFecha(LocalDate fecha);

    List<AsignacionTurno> findByEmpleado_IdAndFechaBetween(Long empleadoId, LocalDate inicio, LocalDate fin);

    Optional<AsignacionTurno> findByEmpleado_IdAndFecha(Long empleadoId, LocalDate fecha);

    Optional<AsignacionTurno> findByEmpleadoIdAndFecha(Long empleadoId, LocalDate fecha);
}
