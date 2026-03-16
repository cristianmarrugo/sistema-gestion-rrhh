package com.example.demo.repository;


import com.example.demo.model.PQRS;
import com.example.demo.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PQRSRepository extends JpaRepository<PQRS, Long> {
    List<PQRS> findByEmpleadoOrderByFechaCreacionDesc(Empleado empleado);
}
