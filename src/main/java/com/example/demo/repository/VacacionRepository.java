package com.example.demo.repository;

import com.example.demo.model.Empleado;
import com.example.demo.model.Vacacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface VacacionRepository extends JpaRepository<Vacacion, Long> {
    @Query("""
    SELECT COUNT(v) > 0
    FROM Vacacion v
    WHERE v.empleado = :empleado
      AND :fecha BETWEEN v.fechaInicio AND v.fechaFin
      AND v.estado = 'APROBADO'
""")
boolean existsVacacionActiva(Empleado empleado, LocalDate fecha);

    @Query("SELECT COUNT(v) FROM Vacacion v WHERE v.estado = 'APROBADO' AND :fecha BETWEEN v.fechaInicio AND v.fechaFin")
    long countVacacionesActivas(LocalDate fecha);

}
