package com.example.demo.repository;



import com.example.demo.model.Documento;
import com.example.demo.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {
    List<Documento> findByEmpleado(Empleado empleado);
}
