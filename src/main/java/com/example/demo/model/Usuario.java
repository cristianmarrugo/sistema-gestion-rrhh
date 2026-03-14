package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "usuarios")
@Data
@Audited
public class Usuario extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    @OneToOne
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;
}
