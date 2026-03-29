package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.envers.Audited;

import java.time.LocalDate;

@Entity
@Table(name = "vacaciones")
@Data
@Audited
public class Vacacion extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private int diasSolicitados;

    @Enumerated(EnumType.STRING)
    private EstadoSolicitud estado;

    private String aprobadoPor;
}

