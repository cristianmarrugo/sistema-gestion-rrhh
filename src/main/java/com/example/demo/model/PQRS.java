package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class PQRS {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String asunto;
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private String tipo; // "PETICION", "QUEJA", "RECLAMO", "SUGERENCIA"
    private String estado; // "PENDIENTE", "EN_REVISION", "RESUELTO"
    private LocalDate fechaCreacion;

    @ManyToOne
    private Empleado empleado;

    private String respuestaRRHH;
}
