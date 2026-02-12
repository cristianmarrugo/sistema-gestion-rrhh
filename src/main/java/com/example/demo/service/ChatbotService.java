package com.example.demo.service;

import com.example.demo.model.Empleado;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final AsistenciaService asistenciaService;
    private final PermisoService permisoService;
    private final VacacionService vacacionService;

    public String procesarMensaje(String mensaje, Empleado empleado) {

        mensaje = mensaje.toLowerCase();

        // SALUDO
        if (mensaje.contains("hola")) {
            return "👋 Hola " + empleado.getNombre()
                    + ", ¿en qué puedo ayudarte hoy?";
        }

        // LLEGADA TARDE
        if (mensaje.contains("tarde")) {
            boolean tarde = asistenciaService.llegoTardeHoy(empleado);
            return tarde
                    ? "⏰ Sí, hoy registras llegada tarde."
                    : "✅ Hoy no registras tardanza.";
        }

        // HORAS EXTRAS DEL MES
        if (mensaje.contains("horas extra")) {
            double horas = asistenciaService
                    .horasExtraMesActual(empleado);

            return "🕒 Este mes llevas "
                    + horas + " horas extras.";
        }

        // PERMISOS
        if (mensaje.contains("permiso")) {
            boolean tiene = permisoService
                    .tienePermisoHoy(empleado);

            return tiene
                    ? "📄 Hoy tienes un permiso aprobado."
                    : "❌ Hoy no tienes permisos.";
        }

        // VACACIONES
        if (mensaje.contains("vacaciones")) {
            boolean enVacaciones =
                    vacacionService.tieneVacacionesHoy(empleado);

            return enVacaciones
                    ? "🏖️ Estás en vacaciones."
                    : "💼 Hoy no estás en vacaciones.";
        }

        // DEFAULT
        return "🤖 No entendí tu consulta. "
                + "Puedes preguntar por tardanzas, horas extra, "
                + "permisos o vacaciones.";
    }
}



