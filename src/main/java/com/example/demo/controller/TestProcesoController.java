package com.example.demo.controller;

import com.example.demo.service.TareaSalidaAutomatica;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test-proceso") // Prefijo de la clase
@CrossOrigin(origins = "*")
public class TestProcesoController {

    @Autowired
    private TareaSalidaAutomatica tareaSalidaAutomatica;

    @GetMapping("/ejecutar-salidas") // Sub-ruta
    public String ejecutarManual() {
        try {
            tareaSalidaAutomatica.cerrarTurnosOlvidados();
            return "✅ Proceso ejecutado. Revisa la consola y la base de datos.";
        } catch (Exception e) {
            return "❌ Error al ejecutar: " + e.getMessage();
        }
    }
}