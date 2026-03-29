package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.envers.Audited;


import java.time.LocalDate;

@Entity
@Audited
@Table(name = "empleados")
@Data
public class Empleado extends Auditable {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String nombre;

    private String apellido;

    @Column(unique = true, nullable = false)
    private String documento;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String pin;

    @Enumerated (EnumType.STRING)
    private Rol rol;

    private String telefono;

    private String direccion;

    private LocalDate fechaNacimiento;

    private Double salario;


    private LocalDate fechaIngreso;
    private boolean activo = true;


    @ManyToOne
    @JoinColumn(name = "cargo_id")
    private Cargo cargo;

    @ManyToOne
    @JoinColumn(name = "horario_id")
    private Horario horario;
}
