package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class HistorialCambio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String entidad; // Ejemplo: "EMPLEADO"
    private Long entidadId; // ID del empleado modificado
    private String accion;  // "CREACIÓN", "EDICIÓN", "ELIMINACIÓN"
    private String detalle; // Ejemplo: "Se cambió el sueldo de 1000 a 1200"

    private LocalDateTime fecha = LocalDateTime.now();

    @ManyToOne
    private Empleado realizadoPor; // El ADMIN/RRHH que hizo el cambio
}
