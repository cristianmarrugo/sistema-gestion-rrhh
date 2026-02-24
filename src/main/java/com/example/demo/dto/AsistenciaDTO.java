package com.example.demo.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO para exportar asistencias a Power BI
 */
public record AsistenciaDTO(
        LocalDate fecha,
        String empleado,
        String documento,
        String cargo,
        LocalTime horaEntrada,
        LocalTime horaSalida,
        String estado,
        double horasTrabajadas,
        int anio,
        int mes,
        String nombreMes,
        String diaSemana,
        int ordenDiaSemana
) {}
