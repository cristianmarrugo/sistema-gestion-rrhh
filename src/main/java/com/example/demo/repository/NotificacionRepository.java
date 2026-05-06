package com.example.demo.repository;


import com.example.demo.model.Notificacion;
import com.example.demo.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    // Busca notificaciones no leídas de un empleado específico, de la más nueva a la más antigua
    List<Notificacion> findByDestinatarioAndLeidaFalseOrderByFechaCreacionDesc(Empleado destinatario);
}
