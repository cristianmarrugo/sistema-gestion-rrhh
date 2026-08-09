package com.example.demo.controller.api;

import com.example.demo.model.Vacacion;
import com.example.demo.service.VacacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vacaciones")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VacacionRestController {

    private final VacacionService vacacionService;

    // Flutter llamará a: GET http://tu-ip:8080/api/vacaciones/1/historial
    @GetMapping("/{id}/historial")
    public List<Vacacion> verHistorial(@PathVariable Long id) {
        return vacacionService.obtenerHistorial(id);
    }
}