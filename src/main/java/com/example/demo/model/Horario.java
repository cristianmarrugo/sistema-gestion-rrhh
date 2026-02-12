package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;


import java.time.LocalTime;

@Entity
@Table(name = "horarios")
@Data
public class Horario {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private LocalTime horaEntrada;
    private LocalTime horaSalida;
    private int toleranciaMinutos;
}
