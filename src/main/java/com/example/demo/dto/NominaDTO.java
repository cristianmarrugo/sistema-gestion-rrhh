package com.example.demo.dto;

public record NominaDTO(
        String empleado,
        String documento,
        String cargo,
        int mes,
        int anio,
        String nombreMes,
        int diasTrabajados,
        int tardanzas,
        double salarioBase,
        double horasExtras,
        double valorHorasExtras,
        double totalPagar
) {}
