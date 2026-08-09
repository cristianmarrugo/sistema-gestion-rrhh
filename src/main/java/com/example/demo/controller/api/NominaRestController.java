package com.example.demo.controller.api;

import com.example.demo.model.Asistencia;
import com.example.demo.model.Empleado;
import com.example.demo.model.EstadoAsistencia;
import com.example.demo.repository.AsistenciaRepository;
import com.example.demo.repository.EmpleadoRepository;
import com.example.demo.repository.HoraExtraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/nomina")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NominaRestController {

    private final AsistenciaRepository asistenciaRepository;
    private final HoraExtraRepository horaExtraRepository;
    private final EmpleadoRepository empleadoRepository;

    // Flutter llamará a: GET http://tu-ip:8080/api/nomina/mi-nomina?empleadoId=1
    @GetMapping("/mi-nomina")
    public Map<String, Object> calcularMiNomina(@RequestParam Long empleadoId) {
        Map<String, Object> response = new HashMap<>();

        Empleado empleado = empleadoRepository.findById(empleadoId).orElse(null);
        if (empleado == null) {
            response.put("success", false);
            response.put("message", "Empleado no encontrado");
            return response;
        }

        LocalDate hoy = LocalDate.now();
        LocalDate primerDia = LocalDate.of(hoy.getYear(), hoy.getMonth(), 1);

        List<Asistencia> asistencias = asistenciaRepository.findByEmpleadoAndFechaBetween(empleado, primerDia, hoy);

        long diasTrabajados = asistencias.stream()
                .filter(a -> a.getEstado() == EstadoAsistencia.NORMAL || a.getEstado() == EstadoAsistencia.TARDE)
                .count();

        long tardanzas = asistencias.stream()
                .filter(a -> a.getEstado() == EstadoAsistencia.TARDE)
                .count();

        long ausencias = asistencias.stream()
                .filter(a -> a.getEstado() == EstadoAsistencia.AUSENTE)
                .count();

        double salarioBase = empleado.getCargo() != null ? empleado.getCargo().getSalarioBase() : 0.0;
        double horasExtras = horaExtraRepository.horasExtraMesActual(empleado);
        double valorExtras = horaExtraRepository.sumHorasExtras(empleado.getId());
        double totalPagar = salarioBase + valorExtras;

        response.put("success", true);
        response.put("mes", hoy.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        response.put("diasTrabajados", diasTrabajados);
        response.put("tardanzas", tardanzas);
        response.put("ausencias", ausencias);
        response.put("salarioBase", salarioBase);
        response.put("horasExtras", horasExtras);
        response.put("valorExtras", valorExtras);
        response.put("totalPagar", totalPagar);
        response.put("asistenciasDetalle", asistencias); // Lista completa de marcas para el histórico móvil

        return response;
    }
}
