package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.envers.Audited;

import java.time.LocalDate;

/**
 * Representa la asignación de un turno específico a un empleado en una fecha concreta.
 * Esto es el "cuadrante mensual" día por día.
 */
@Entity
@Table(name = "asignaciones_turno")
@Data
@ToString(exclude = "empleado")
@Audited
public class AsignacionTurno extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @ManyToOne(fetch = FetchType.EAGER)  // ← Cambiar LAZY por EAGER
    @JoinColumn(name = "turno_id")
    private Turno turno; // NULL si es día libre

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    private TipoAsignacion tipo = TipoAsignacion.NORMAL;

    private String observaciones;

    // Índice para búsquedas rápidas
    // CREATE INDEX idx_empleado_fecha ON asignaciones_turno(empleado_id, fecha);
}
