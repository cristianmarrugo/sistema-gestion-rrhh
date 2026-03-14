package com.example.demo.repository;

import com.example.demo.model.HistorialCambio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HistorialCambioRepository extends JpaRepository<HistorialCambio, Long> {

    /**
     * Obtiene todo el historial ordenado desde el más nuevo al más antiguo.
     * Ideal para la vista general de auditoría.
     */
    List<HistorialCambio> findAllByOrderByFechaDesc();

    /**
     * Filtra el historial de un empleado específico.
     * Útil si quieres ver los cambios solo de una persona.
     */
    List<HistorialCambio> findByEntidadAndEntidadIdOrderByFechaDesc(String entidad, Long entidadId);
}
