package com.example.demo.controller;

import com.example.demo.model.Empleado;
import com.example.demo.service.AsistenciaService;
import com.example.demo.service.TurnoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.openai.OpenAiChatModel; // Asegúrate de importar esta
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final OpenAiChatModel chatModel; // Cambiamos ChatClient por el Model directo
    private final AsistenciaService asistenciaService;
    private final TurnoService turnoService;

    @PostMapping("/mensaje")
    public ResponseEntity<String> procesarMensaje(@RequestBody Map<String, String> request, HttpSession session) {
        Empleado empleado = (Empleado) session.getAttribute("empleado");
        if (empleado == null) return ResponseEntity.status(401).body("Debes iniciar sesión");

        String mensajeUsuario = request.get("mensaje");
        String datosContexto = obtenerContextoEmpleado(empleado);

        try {
            String systemInstruction = String.format("""
        Eres el 'Asistente Virtual de RRHH' de nuestra empresa. 
        TU IDENTIDAD: Tu nombre es richard, eres una IA de soporte.
        DATOS DEL USUARIO ACTUAL: %s. 
        
        REGLAS CRÍTICAS:
        1. Tu única función es ayudar con temas de RRHH (tardanzas, horas extra, horarios, nómina).
        2. Si el usuario te pregunta sobre temas ajenos (fútbol, política, cocina, mundial, etc.), responde cortésmente: 
           "Lo siento, solo puedo ayudarte con consultas relacionadas con Recursos Humanos y tu actividad en la empresa."
        3. Dirígete al usuario por su nombre cuando lo saludes.
        4. Usa la información de las tardanzas y horas extra que te proporciono para dar respuestas exactas.
        5. Sé breve, profesional y usa emojis.
        
        Tu fuente de verdad es el 'Cuadrante de Turnos'.\s
        CONTEXTO DEL EMPLEADO:
                       \s
        INSTRUCCIONES:
        1. Si el empleado pregunta por su horario, revisa la información del turno asignado para HOY que te proporcioné.
        2. Si no hay un turno asignado en el contexto, indícale que no aparece en el cuadrante de hoy y debe contactar a su supervisor.
        """, datosContexto);

            SystemMessage systemMessage = new SystemMessage(systemInstruction);
            UserMessage userMessage = new UserMessage(mensajeUsuario);

            Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
            return ResponseEntity.ok(chatModel.call(prompt).getResult().getOutput().getContent());
        } catch (Exception e) {
            // LOG del error para ti
            System.err.println("Error en OpenAI: " + e.getMessage());

            // Respuesta de respaldo para el usuario
            return ResponseEntity.ok("🤖 Lo siento, estoy teniendo problemas de conexión con mis circuitos cerebrales (OpenAI Quota). " +
                    "Por ahora, por favor consulta directamente en la oficina de RRHH.");
        }
    }

    private String obtenerContextoEmpleado(Empleado emp) {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);

        // 1. Obtener el turno de HOY desde el cuadrante/asignación
        // Supongamos que tienes un método que busca la asignación por empleado y fecha
        var turnoActual = turnoService.buscarTurnoPorEmpleadoYFecha(emp.getId(), hoy);

        String infoTurno = (turnoActual != null)
                ? String.format("Hoy tienes el turno '%s' de %s a %s.",
                turnoActual.getTurno().getNombre(),
                turnoActual.getTurno().getHoraEntrada(),
                turnoActual.getTurno().getHoraSalida())
                : "No tienes un turno asignado para hoy en el cuadrante.";

        // 2. Obtener métricas de asistencia (Tardanzas y Horas Extra)
        long tardanzas = asistenciaService.obtenerPorEmpleadoYRangoFechas(emp.getId(), inicioMes, hoy)
                .stream().filter(a -> "TARDE".equals(a.getEstado())).count();

        double horasExtra = asistenciaService.horasExtraMesActual(emp);



        // 3. Construir el "paquete" de información para la IA
        return String.format(
                "Usuario: %s. %s. Tardanzas en el mes actual: %d. Horas extra acumuladas: %.2f. " +
                        "Cargo: %s.",
                emp.getNombre(),
                infoTurno,
                tardanzas,
                horasExtra,
                emp.getCargo().getNombre()
        );
    }
}




