package com.example.demo.repository;

import com.example.demo.model.Empleado;
import com.example.demo.model.HoraExtra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface HoraExtraRepository extends JpaRepository<HoraExtra, Long> {

    @Query("""
        SELECT COALESCE(SUM(h.valor), 0)
        FROM HoraExtra h
        WHERE h.empleado.id = :empleadoId
    """)
    double sumHorasExtras(Long empleadoId);

    @Query("""
    SELECT COALESCE(SUM(h.horas), 0)
    FROM HoraExtra h
    WHERE h.empleado = :empleado
      AND MONTH(h.fecha) = MONTH(CURRENT_DATE)
      AND YEAR(h.fecha) = YEAR(CURRENT_DATE)
""")
    double horasExtraMesActual(Empleado empleado);
}

