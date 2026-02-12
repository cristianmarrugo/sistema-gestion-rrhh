package com.example.demo.controller;

import com.example.demo.dto.ChatRequest;
import com.example.demo.model.Empleado;
import com.example.demo.service.ChatbotService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping(
            value = "/mensaje",
            consumes = "application/json",
            produces = "text/plain"
    )
    public String enviarMensaje(
            @RequestBody ChatRequest request,
            HttpSession session) {

        Empleado empleado =
                (Empleado) session.getAttribute("empleado");

        if (empleado == null) {
            return "⚠️ Tu sesión expiró. Ingresa tu PIN nuevamente.";
        }

        return chatbotService.procesarMensaje(
                request.getMensaje(),
                empleado
        );
    }
}






