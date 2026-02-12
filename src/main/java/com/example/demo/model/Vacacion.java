package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "vacaciones")
@Data
public class Vacacion {

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
}

