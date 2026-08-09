package com.example.demo.controller.api;

import com.example.demo.model.Permiso;
import com.example.demo.service.PermisoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permisos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PermisoRestController {

    private final PermisoService permisoService;

    // Flutter llamará a: GET http://tu-ip:8080/api/permisos/1/historial
    @GetMapping("/{id}/historial")
    public List<Permiso> obtenerHistorial(@PathVariable Long id) {
        return permisoService.obtenerHistorial(id);
    }
}