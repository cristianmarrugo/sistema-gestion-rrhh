package com.example.demo.dto;

public record ResumenMensualDTO(
        int anio,
        int mes,
        String nombreMes,
        long totalRegistros,
        long totalNormal,
        long totalTarde,
        long totalAusente,
        long totalPermiso,
        double porcentajePuntualidad
) {}
