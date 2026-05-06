package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Notificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Empleado destinatario;

    private String mensaje;
    private String url; // Ejemplo: "/admin/permisos/pendientes"
    private boolean leida = false;
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}