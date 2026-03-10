package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalTime;

@Entity
@Table(name = "turnos")
@Data
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre; // "Mañana", "Tarde", "Noche", "Administrativo"

    @Column(nullable = false)
    private String codigo; // "M", "T", "N", "A" (para cuadrantes)

    private LocalTime horaEntrada;
    private LocalTime horaSalida;

    private int toleranciaMinutos = 15;

    private String color; // "#10B981" para identificar visualmente

    private String descripcion;

    // Días en los que aplica este turno (NULL = todos los días)
    private Boolean lunes = true;
    private Boolean martes = true;
    private Boolean miercoles = true;
    private Boolean jueves = true;
    private Boolean viernes = true;
    private Boolean sabado = false;
    private Boolean domingo = false;
}
