package com.example.demo.controller;

import com.example.demo.model.Empleado;
import com.example.demo.repository.EmpleadoRepository;
import com.example.demo.repository.HoraExtraRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.PrintWriter;
import java.util.List;

@RestController
@RequestMapping("/api/nomina")
public class NominaController {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private HoraExtraRepository horaExtraRepository;

    @GetMapping("/exportar")
    public void exportarNomina(HttpServletResponse response) throws Exception {

        response.setContentType("text/csv");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=nomina.csv"
        );

        PrintWriter writer = response.getWriter();
        writer.println("Empleado,Salario Base,Horas Extras,Total");

        List<Empleado> empleados = empleadoRepository.findAll();

        for (Empleado e : empleados) {

            double salario = e.getCargo().getSalarioBase();
            double extras = horaExtraRepository.sumHorasExtras(e.getId());

            writer.println(
                    e.getNombre() + " " + e.getApellido() + "," +
                            salario + "," +
                            extras + "," +
                            (salario + extras)
            );
        }

        writer.flush();
    }
}

