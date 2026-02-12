package com.example.demo.controller;


import com.example.demo.model.Asistencia;
import com.example.demo.service.AsistenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/asistencias")
public class AsistenciaController {

    @Autowired
    private AsistenciaService asistenciaService;

    @PostMapping("/marcar")
    public ResponseEntity<Asistencia> marcarAsistencia(@RequestParam String pin) {
        Asistencia asistencia = asistenciaService.marcarAsistencia(pin);
        return ResponseEntity.ok(asistencia);
    }
}

