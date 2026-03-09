package com.example.demo.controller;

import com.example.demo.model.Asistencia;
import com.example.demo.model.Empleado;
import com.example.demo.model.EstadoAsistencia;
import com.example.demo.repository.AsistenciaRepository;
import com.example.demo.repository.HoraExtraRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/nomina")
@RequiredArgsConstructor
public class NominaViewController {

    private final AsistenciaRepository asistenciaRepository;
    private final HoraExtraRepository horaExtraRepository;

    /**
     * Vista para que empleados vean su propia nómina
     */
    @GetMapping("/mi-nomina")
    public String verMiNomina(HttpSession session, Model model) {
        Empleado empleado = (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return "redirect:/login";
        }

        // Calcular datos del mes actual
        LocalDate hoy = LocalDate.now();
        LocalDate primerDia = LocalDate.of(hoy.getYear(), hoy.getMonth(), 1);
        LocalDate fechaHasta = hoy;

        // Obtener asistencias
        List<Asistencia> asistencias = asistenciaRepository
                .findByEmpleadoAndFechaBetween(empleado, primerDia, fechaHasta);

        // Calcular días trabajados
        long diasTrabajados = asistencias.stream()
                .filter(a -> a.getEstado() == EstadoAsistencia.NORMAL ||
                        a.getEstado() == EstadoAsistencia.TARDE)
                .count();

        // Calcular tardanzas
        long tardanzas = asistencias.stream()
                .filter(a -> a.getEstado() == EstadoAsistencia.TARDE)
                .count();

        // Calcular ausencias
        long ausencias = asistencias.stream()
                .filter(a -> a.getEstado() == EstadoAsistencia.AUSENTE)
                .count();

        // Salarios
        double salarioBase = empleado.getCargo() != null ?
                empleado.getCargo().getSalarioBase() : 0.0;

        // Horas extras
        double horasExtras = horaExtraRepository.horasExtraMesActual(empleado);
        double valorExtras = horaExtraRepository.sumHorasExtras(empleado.getId());

        // Total a pagar
        double totalPagar = salarioBase + valorExtras;

        // Agregar al modelo
        model.addAttribute("empleado", empleado);
        model.addAttribute("mes", hoy.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        model.addAttribute("diasTrabajados", diasTrabajados);
        model.addAttribute("tardanzas", tardanzas);
        model.addAttribute("ausencias", ausencias);
        model.addAttribute("salarioBase", salarioBase);
        model.addAttribute("horasExtras", horasExtras);
        model.addAttribute("valorExtras", valorExtras);
        model.addAttribute("totalPagar", totalPagar);
        model.addAttribute("asistencias", asistencias);

        return "nomina/mi-nomina";
    }
}
