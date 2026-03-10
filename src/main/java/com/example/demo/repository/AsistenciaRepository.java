package com.example.demo.repository;

import com.example.demo.model.Asistencia;
import com.example.demo.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {
    Optional<Asistencia> findByEmpleadoAndFecha(Empleado empleado, LocalDate fecha);

    List<Asistencia> findByEmpleadoAndFechaBetween(Empleado empleado, LocalDate fechaInicio, LocalDate fechaFin);
}
