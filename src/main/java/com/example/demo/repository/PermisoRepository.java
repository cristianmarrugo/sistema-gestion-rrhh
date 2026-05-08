package com.example.demo.repository;

import com.example.demo.model.Empleado;
import com.example.demo.model.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("SELECT COUNT(p) FROM Permiso p WHERE p.estado = 'APROBADO' AND :fecha BETWEEN p.fechaInicio AND p.fechaFin")
    long countPermisosActivos(LocalDate fecha);

    @Query("SELECT COUNT(p) > 0 FROM Permiso p WHERE p.empleado = :emp " +
            "AND p.estado != 'RECHAZADO' " +
            "AND ((:inicio BETWEEN p.fechaInicio AND p.fechaFin) " +
            "OR (:fin BETWEEN p.fechaInicio AND p.fechaFin) " +
            "OR (p.fechaInicio BETWEEN :inicio AND :fin))")
    boolean existeCruceDeFechas(@Param("emp") Empleado emp,
                                @Param("inicio") LocalDate inicio,
                                @Param("fin") LocalDate fin);
}