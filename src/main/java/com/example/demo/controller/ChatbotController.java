package com.example.demo.controller;

import com.example.demo.model.Empleado;
import com.example.demo.service.AsistenciaService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final AsistenciaService asistenciaService;

    @PostMapping("/mensaje")
    public ResponseEntity<String> procesarMensaje(@RequestBody Map<String, String> request,
                                                  HttpSession session) {

        Empleado empleado = (Empleado) session.getAttribute("empleado");
        if (empleado == null) {
            return ResponseEntity.status(401).body("Debes iniciar sesión");
        }

        String mensaje = request.get("mensaje").toLowerCase().trim();
        String respuesta = generarRespuesta(mensaje, empleado);

        return ResponseEntity.ok(respuesta);
    }

    private String generarRespuesta(String mensaje, Empleado empleado) {

        // 1. CONSULTAS SOBRE TARDANZAS
        if (mensaje.contains("tardanza") || mensaje.contains("tarde") ||
                mensaje.contains("cuantas veces") || mensaje.contains("llegué tarde")) {

            LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
            LocalDate hoy = LocalDate.now();

            long tardanzas = asistenciaService.obtenerPorEmpleadoYRangoFechas(
                            empleado.getId(), inicioMes, hoy
                    ).stream()
                    .filter(a -> "TARDE".equals(a.getEstado()))
                    .count();

            if (tardanzas == 0) {
                return "🎉 ¡Excelente! No tienes tardanzas este mes. Sigue así.";
            } else if (tardanzas <= 2) {
                return "⚠️ Tienes " + tardanzas + " tardanza(s) este mes. " +
                        "Recuerda llegar a tiempo para mantener tu récord impecable.";
            } else {
                return "⚠️ Tienes " + tardanzas + " tardanzas este mes. " +
                        "Te recomendamos revisar tu horario y planificar mejor tus llegadas. " +
                        "Si tienes dificultades, habla con RRHH.";
            }
        }

        // 2. CONSULTAS SOBRE HORAS EXTRA
        if (mensaje.contains("horas extra") || mensaje.contains("horas adicionales") ||
                mensaje.contains("tiempo extra")) {

            LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
            LocalDate hoy = LocalDate.now();

            double horasExtra = asistenciaService.horasExtraMesActual(empleado);

            if (horasExtra == 0) {
                return "⏱️ No tienes horas extra registradas este mes. " +
                        "Recuerda marcar tu salida después de tu horario si trabajas tiempo adicional.";
            } else {
                return String.format("⏱️ Tienes %.2f horas extra este mes. " +
                        "Estas serán incluidas en tu nómina. ¡Buen trabajo!", horasExtra);
            }
        }

        // 3. CONSULTAS SOBRE PERMISOS
        if (mensaje.contains("permiso") || mensaje.contains("solicitar") ||
                mensaje.contains("solicitud")) {

            return "📄 Para solicitar un permiso:\n\n" +
                    "1. Ve a la sección 'Permisos' en el menú\n" +
                    "2. Click en 'Solicitar Permiso'\n" +
                    "3. Completa el formulario con fecha y motivo\n" +
                    "4. RRHH revisará tu solicitud\n\n" +
                    "💡 Tip: Solicita con anticipación para mejor aprobación.";
        }

        // 4. CONSULTAS SOBRE VACACIONES
        if (mensaje.contains("vacacion") || mensaje.contains("descanso")) {
            return "🏖️ Para solicitar vacaciones:\n\n" +
                    "1. Ve a la sección 'Vacaciones' en el menú\n" +
                    "2. Verifica tus días disponibles\n" +
                    "3. Solicita con al menos 15 días de anticipación\n" +
                    "4. RRHH aprobará según disponibilidad\n\n" +
                    "¿Necesitas saber cuántos días tienes? Contáctame.";
        }

        // 5. CONSULTAS SOBRE HORARIO
        if (mensaje.contains("horario") || mensaje.contains("entrada") ||
                mensaje.contains("salida") || mensaje.contains("hora")) {

            if (empleado.getHorario() == null) {
                return "⚠️ No tienes un horario asignado. " +
                        "Contacta con RRHH para que te asignen uno.";
            }

            return String.format("🕐 Tu horario es:\n\n" +
                            "Entrada: %s\n" +
                            "Salida: %s\n" +
                            "Tolerancia: %d minutos\n\n" +
                            "Recuerda marcar tu entrada y salida todos los días.",
                    empleado.getHorario().getHoraEntrada(),
                    empleado.getHorario().getHoraSalida(),
                    empleado.getHorario().getToleranciaMinutos());
        }

        // 6. CONSULTAS SOBRE NÓMINA
        if (mensaje.contains("nomina") || mensaje.contains("salario") ||
                mensaje.contains("pago") || mensaje.contains("sueldo")) {

            return "💰 Información de nómina:\n\n" +
                    "• Puedes ver tu nómina en la sección 'Mi Nómina'\n" +
                    "• Los pagos se realizan el día 30 de cada mes\n" +
                    "• Incluye: salario base + horas extra\n\n" +
                    "Si tienes dudas sobre tu pago, contacta a RRHH.";
        }

        // 7. CONSULTAS SOBRE ASISTENCIA
        if (mensaje.contains("asistencia") || mensaje.contains("marcar") ||
                mensaje.contains("registro") || mensaje.contains("olvidé")) {

            return "⏱️ Sobre asistencia:\n\n" +
                    "• Marca tu entrada al llegar\n" +
                    "• Marca tu salida al irte\n" +
                    "• Si olvidaste marcar, contacta a RRHH\n" +
                    "• Puedes ver tu historial en 'Mi Asistencia'\n\n" +
                    "💡 Recuerda: la salida automática solo registra tu horario, " +
                    "no tus horas extra.";
        }

        // 8. CONSULTAS SOBRE AUSENCIAS
        if (mensaje.contains("ausencia") || mensaje.contains("falta") ||
                mensaje.contains("no pude") || mensaje.contains("no asistí")) {

            LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
            LocalDate hoy = LocalDate.now();

            long ausencias = asistenciaService.obtenerPorEmpleadoYRangoFechas(
                            empleado.getId(), inicioMes, hoy
                    ).stream()
                    .filter(a -> "AUSENTE".equals(a.getEstado()))
                    .count();

            return String.format("⚠️ Tienes %d ausencia(s) este mes.\n\n" +
                    "Si tuviste un inconveniente:\n" +
                    "1. Solicita un permiso retroactivo\n" +
                    "2. Contacta con RRHH para justificar\n\n" +
                    "Las ausencias no justificadas afectan tu nómina.", ausencias);
        }

        // 9. SALUDO
        if (mensaje.contains("hola") || mensaje.contains("buenos") ||
                mensaje.contains("buenas")) {
            return "¡Hola " + empleado.getNombre() + "! 👋\n\n" +
                    "Soy tu asistente virtual de RRHH. Puedo ayudarte con:\n\n" +
                    "• Consultar tus tardanzas\n" +
                    "• Ver tus horas extra\n" +
                    "• Información sobre permisos y vacaciones\n" +
                    "• Tu horario de trabajo\n" +
                    "• Estado de tu nómina\n\n" +
                    "¿En qué puedo ayudarte?";
        }

        // 10. AYUDA
        if (mensaje.contains("ayuda") || mensaje.contains("qué puedes") ||
                mensaje.contains("que haces")) {
            return "🤖 Puedo ayudarte con:\n\n" +
                    "📊 Tardanzas y asistencias\n" +
                    "⏱️ Horas extra trabajadas\n" +
                    "📄 Solicitudes de permisos\n" +
                    "🏖️ Vacaciones\n" +
                    "🕐 Horarios de trabajo\n" +
                    "💰 Información de nómina\n" +
                    "⚠️ Ausencias y faltas\n\n" +
                    "Pregúntame cualquier cosa sobre estos temas.";
        }

        if (mensaje.contains("gracias") || mensaje.contains("muchas")){
            return "Un gusto servirte 👋";
        }

        // RESPUESTA POR DEFECTO
        return "🤔 No estoy seguro de entender tu pregunta.\n\n" +
                "Puedo ayudarte con:\n" +
                "• Tardanzas y asistencias\n" +
                "• Horas extra\n" +
                "• Permisos y vacaciones\n" +
                "• Horarios\n" +
                "• Nómina\n\n" +
                "Escribe 'ayuda' para ver todas las opciones.";
    }
}






