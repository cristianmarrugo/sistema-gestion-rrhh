package com.example.demo.controller.api;

import com.example.demo.model.HistorialCambio;
import com.example.demo.repository.HistorialCambioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*") // Permite la conexión desde Flutter
public class AdminRestController {

    @Autowired
    private HistorialCambioRepository historialCambioRepository;

    // Flutter llamará a: GET http://tu-ip:8080/api/admin/historial
    @GetMapping("/historial")
    public List<HistorialCambio> obtenerHistorial() {
        // Retorna la lista de logs directamente como JSON al celular
        return historialCambioRepository.findAllByOrderByFechaDesc();
    }
}
