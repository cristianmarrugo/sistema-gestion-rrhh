package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;


import java.time.LocalDate;

@Entity
@Table(name = "empleados")
@Data
public class Empleado {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String nombre;
    private String apellido;
    private String documento;
    private String email;
    private String pin;
    @Enumerated (EnumType.STRING)
    private Rol rol;


    private LocalDate fechaIngreso;
    private boolean activo = true;


    @ManyToOne
    @JoinColumn(name = "cargo_id")
    private Cargo cargo;

    @ManyToOne
    @JoinColumn(name = "horario_id")
    private Horario horario;
}
