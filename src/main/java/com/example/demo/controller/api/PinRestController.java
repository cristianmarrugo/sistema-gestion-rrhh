package com.example.demo.controller.api;

import com.example.demo.model.Empleado;
import com.example.demo.repository.EmpleadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/pin")
@CrossOrigin(origins = "*")
public class PinRestController {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    // Flutter llamará a: POST http://tu-ip:8080/api/pin?pin=1234
    @PostMapping
    public Map<String, Object> procesarLoginPorPin(@RequestParam String pin) {
        Map<String, Object> response = new HashMap<>();
        Optional<Empleado> empleadoOpt = empleadoRepository.findByPin(pin);

        if (empleadoOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "PIN incorrecto");
            return response;
        }

        Empleado empleado = empleadoOpt.get();

        if (!empleado.isActivo()) {
            response.put("success", false);
            response.put("message", "Usuario inactivo. Contacte con RRHH");
            return response;
        }

        response.put("success", true);
        response.put("empleado", empleado);
        response.put("rol", empleado.getRol());
        return response;
    }
}
