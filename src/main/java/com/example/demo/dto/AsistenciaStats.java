package com.example.demo.dto;

import lombok.Data;

@Data
public class AsistenciaStats {
    private long totalNormal;
    private long totalTarde;
    private long totalAusente;
    private long totalPermiso;
    private long totalVacaciones;

    // Constructores, Getters y Setters
    public AsistenciaStats(long normal, long tarde, long ausente, long permiso, long vacaciones) {
        this.totalNormal = normal;
        this.totalTarde = tarde;
        this.totalAusente = ausente;
        this.totalPermiso = permiso;
        this.totalVacaciones = vacaciones;
    }

    // ... Getters
}