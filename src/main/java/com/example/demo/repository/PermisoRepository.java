package com.example.demo.repository;

import com.example.demo.model.Empleado;
import com.example.demo.model.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface PermisoRepository extends JpaRepository<Permiso, Long> {
    @Query("""
                SELECT COUNT(p) > 0
                FROM Permiso p
                WHERE p.empleado = :empleado
                  AND :fecha BETWEEN p.fechaInicio AND p.fechaFin
                  AND p.estado = 'APROBADO'
            """)

    boolean existsPermisoActivo(Empleado empleado, LocalDate fecha);
}