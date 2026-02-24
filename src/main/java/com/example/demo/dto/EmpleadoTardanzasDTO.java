package com.example.demo.dto;

public record EmpleadoTardanzasDTO(
        String empleado,
        String documento,
        String cargo,
        long totalAsistencias,
        long totalTardanzas,
        long totalAusencias,
        double porcentajeTardanzas
) {}
