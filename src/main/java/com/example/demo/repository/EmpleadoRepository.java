package com.example.demo.repository;

import com.example.demo.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    Optional<Empleado> findByPin(String pin);

    Optional<Empleado> findByDocumento(String documento);

    Optional<Object> findByEmail(String email);

    long countByActivoTrue();

    @Query("SELECT COUNT(e) FROM Empleado e WHERE e.activo = true AND e.id NOT IN " +
            "(SELECT a.empleado.id FROM AsignacionTurno a WHERE a.fecha = :fecha AND a.turno IS NOT NULL)")
    long countEmpleadosSinTurnoHoy(LocalDate fecha);
}
