package com.example.demo.controller.api;

import com.example.demo.dto.AnalisisIA;
import com.example.demo.service.RedNeuronalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ia-prediccion")
@CrossOrigin(origins = "*")
public class IARestController {

    @Autowired
    private RedNeuronalService rnService;

    // Flutter llamará a: POST http://tu-ip:8080/api/ia-prediccion/analizar
    @PostMapping("/analizar")
    public AnalisisIA procesarIA(@RequestBody AnalisisIA analisis) {

        String respuestaRaw = rnService.ejecutarPrediccion(
                analisis.getDiaSemana(),
                analisis.getMinutosRetraso(),
                analisis.getReferenciaUbicacion()
        );

        String[] partes = respuestaRaw.split(",");

        if (partes.length >= 2) {
            analisis.setResultadoPrediccion(partes[0]); // "PUNTUAL" o "TARDE"
            analisis.setProbabilidad(partes[1]);        // "95.5"
        } else {
            analisis.setResultadoPrediccion(partes[0]);
            analisis.setProbabilidad("0");
        }

        return analisis; // Flutter recibe el objeto mapeado completo en JSON
    }
}