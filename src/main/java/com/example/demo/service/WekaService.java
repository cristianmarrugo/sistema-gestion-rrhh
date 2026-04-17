package com.example.demo.service;

import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;
import weka.core.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class WekaService {

    private Classifier modelo;


        public WekaService() {
            try {
                // Carga el archivo .model desde la carpeta resources
                InputStream is = getClass().getClassLoader().getResourceAsStream("modelo_tardanzas.model");
                if (is != null) {
                    this.modelo = (Classifier) SerializationHelper.read(is);
                    System.out.println("✅ Modelo de Weka cargado con éxito.");
                } else {
                    System.err.println("❌ No se encontró el archivo modelo_tardanzas.model en resources.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public String predecir(int diaSemana, double minutosRetraso, String ubicacion) throws Exception {
            // 1. Definir Atributos NUMÉRICOS
            Attribute attrDia = new Attribute("dia_semana");
            Attribute attrMinutos = new Attribute("minutos_retraso");

            // 2. Definir Atributo NOMINAL (Ubicación)
            // Deben ser exactamente los mismos que en tu .arff
            ArrayList<String> barrios = new ArrayList<>(Arrays.asList(
                    "Boquilla carrera 9", "Boquilla carrera 9 # 53-29", "Boquilla",
                    "Santa maria", "Villa grande", "Boquila",
                    "DIAGONAL 21 E 54 44, CARTAGENA, BOLIVAR                                         "
            ));
            Attribute attrUbicacion = new Attribute("referencia_ubicacion", barrios);

            // 3. Definir Clase NOMINAL (Resultado)
            ArrayList<String> clases = new ArrayList<>(Arrays.asList("TARDE", "PUNTUAL"));
            Attribute attrClase = new Attribute("resultado", clases);

            // 4. Crear el Dataset (Estructura de la tabla)
            ArrayList<Attribute> listaAtributos = new ArrayList<>();
            listaAtributos.add(attrDia);
            listaAtributos.add(attrMinutos);
            listaAtributos.add(attrUbicacion);
            listaAtributos.add(attrClase);

            Instances data = new Instances("PrediccionActual", listaAtributos, 0);
            data.setClassIndex(data.numAttributes() - 1);

            // 5. Crear la instancia a predecir
            DenseInstance instancia = new DenseInstance(4);
            instancia.setDataset(data);
            instancia.setValue(attrDia, diaSemana);
            instancia.setValue(attrMinutos, minutosRetraso);
            instancia.setValue(attrUbicacion, ubicacion);

            // 6. Realizar la clasificación
            double resultadoIdx = modelo.classifyInstance(instancia);
            return data.classAttribute().value((int) resultadoIdx);
        }

    public Map<String, Object> predecirConProbabilidad(int diaSemana, double minutosRetraso, String ubicacion) throws Exception {
        // 1. Definir Atributos NUMÉRICOS
        Attribute attrDia = new Attribute("dia_semana");
        Attribute attrMinutos = new Attribute("minutos_retraso");

        // 2. Definir Atributo NOMINAL (Ubicación)
        // Usamos los barrios definidos en tu archivo ARFF
        ArrayList<String> barrios = new ArrayList<>(Arrays.asList(
                "Boquilla carrera 9", "Boquilla carrera 9 # 53-29", "Boquilla",
                "Santa maria", "Villa grande", "Boquila",
                "DIAGONAL 21 E 54 44, CARTAGENA, BOLIVAR                                         "
        ));
        Attribute attrUbicacion = new Attribute("referencia_ubicacion", barrios);

        // 3. Definir Clase NOMINAL (Resultado)
        // Importante: El orden debe ser el mismo que en tu ARFF {TARDE, PUNTUAL}
        ArrayList<String> clases = new ArrayList<>(Arrays.asList("TARDE", "PUNTUAL"));
        Attribute attrClase = new Attribute("resultado", clases);

        // 4. Crear la estructura de datos
        ArrayList<Attribute> listaAtributos = new ArrayList<>();
        listaAtributos.add(attrDia);
        listaAtributos.add(attrMinutos);
        listaAtributos.add(attrUbicacion);
        listaAtributos.add(attrClase);

        Instances data = new Instances("PrediccionActual", listaAtributos, 0);
        data.setClassIndex(data.numAttributes() - 1);

        // 5. Crear la instancia a predecir (ESTO ES LO QUE TE FALTABA)
        DenseInstance instancia = new DenseInstance(4);
        instancia.setDataset(data);
        instancia.setValue(attrDia, (double) diaSemana);
        instancia.setValue(attrMinutos, minutosRetraso);
        instancia.setValue(attrUbicacion, ubicacion);

        // 6. Calcular probabilidades
        double[] porcentajes = modelo.distributionForInstance(instancia);

        // Índice 0 = TARDE, Índice 1 = PUNTUAL (según la lista 'clases')
        double probTarde = porcentajes[0] * 100;
        double probPuntual = porcentajes[1] * 100;

        String resultadoFinal = (probTarde > probPuntual) ? "TARDE" : "PUNTUAL";
        double confianza = Math.max(probTarde, probPuntual);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("resultado", resultadoFinal);
        respuesta.put("confianza", String.format("%.2f", confianza));

        return respuesta;
    }
}
