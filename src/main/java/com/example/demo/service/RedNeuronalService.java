package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
public class RedNeuronalService {

    @Value("${PYTHON_EXE_PATH:./venv/Scripts/python.exe}")
    private String pythonExe;

    public String ejecutarPrediccion(int dia, int minutos, String ubicacion) {
        try {
            // RUTA ABSOLUTA al intérprete de tu venv
            // RUTA RELATIVA al script desde la raíz del proyecto
            String scriptPath = "src/scripts/python/retrasos.py";

            // Configuración del proceso
            ProcessBuilder pb = new ProcessBuilder(
                    pythonExe,
                    scriptPath,
                    String.valueOf(dia),
                    String.valueOf(minutos),
                    ubicacion.trim()
            );

            Process p = pb.start();
            // Especificamos UTF-8 para evitar problemas con tildes en las ubicaciones
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"));
            String resultado = in.readLine();

            // --- MODIFICACIÓN AQUÍ ---
            // Si el resultado no es nulo, lo devolvemos tal cual (ej: "PUNTUAL,95.5")
            // Si es nulo, devolvemos un formato compatible para que el split del controller no falle
            return (resultado != null) ? resultado.trim() : "Error en proceso,0";

        } catch (Exception e) {
            return "Error: " + e.getMessage() + ",0";
        }
    }
}