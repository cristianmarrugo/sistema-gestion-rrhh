package com.example.demo.controller.api;

import com.example.demo.model.Notificacion;
import com.example.demo.repository.NotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
@CrossOrigin(origins = "*")
public class NotificacionRestController {

    @Autowired
    private NotificacionRepository repository;

    // Flutter llamará a: POST http://tu-ip:8080/api/notificaciones/leer/5?empleadoId=1
    @PostMapping("/leer/{id}")
    public Map<String, Object> marcarComoLeida(@PathVariable Long id, @RequestParam Long empleadoId) {
        Map<String, Object> response = new HashMap<>();

        Notificacion n = repository.findById(id).orElseThrow();

        if (!n.getDestinatario().getId().equals(empleadoId)) {
            response.put("success", false);
            response.put("message", "Acceso denegado");
            return response;
        }

        n.setLeida(true);
        repository.save(n);

        response.put("success", true);
        response.put("destinoUrl", n.getUrl()); // Flutter leerá este String para saber a dónde llevar al usuario
        return response;
    }
}
