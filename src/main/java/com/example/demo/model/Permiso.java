package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.envers.Audited;

import java.time.LocalDate;

@Entity
@Table(name = "permisos")
@Data
@Audited
public class Permiso extends Auditable{


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;


    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String motivo;


    @Enumerated(EnumType.STRING)
    private EstadoSolicitud estado;

    private String aprobadoPor;
}
