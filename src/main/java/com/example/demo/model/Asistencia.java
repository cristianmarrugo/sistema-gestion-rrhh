package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration; // Importa esto explícitamente

@Entity
@Table(name = "asistencias")
@Data
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;

    private LocalDate fecha;
    private LocalTime horaEntrada;
    private LocalTime horaSalida;

    @Enumerated(EnumType.STRING)
    private EstadoAsistencia estado;

    @Column(name = "observacion")
    private String observacion;

    // Cálculo dinámico para el reporte
    @Transient
    public Double getHorasTotales() {
        if (this.horaEntrada == null || this.horaSalida == null) {
            return 0.0;
        }
        try {
            long minutos = java.time.Duration.between(this.horaEntrada, this.horaSalida).toMinutes();
            return minutos / 60.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    // Alias para facilitar el acceso desde Thymeleaf
    @Transient
    public Double getHorasExtras() {
        // Si ya tienes un campo o lógica de extras, puedes ponerla aquí
        // Por ahora devolvemos 0.0 si es nulo para evitar el error en el HTML
        return 0.0;
    }
}
