package com.example.demo.dto;

import lombok.Data;

@Data
public class AnalisisIA {

    private int diaSemana; // 1-7
    private int minutosRetraso;
    private String referenciaUbicacion;
    private String resultadoPrediccion;
    private String probabilidad;
    // Getters y Setters...
}
