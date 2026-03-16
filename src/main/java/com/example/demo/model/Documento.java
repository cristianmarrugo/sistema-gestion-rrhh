package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Documento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;      // Ejemplo: "Cedula_Juan.pdf"
    private String tipo;        // "CEDULA", "CONTRATO", "MEDICO"
    private String rutaArchivo; // "/uploads/documentos/123_cedula.pdf"
    private LocalDate fechaSubida;

    @ManyToOne
    private Empleado empleado;
}