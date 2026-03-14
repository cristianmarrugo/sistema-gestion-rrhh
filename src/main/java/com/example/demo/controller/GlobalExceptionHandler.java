package com.example.demo.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 1. Capturar errores 500 (Errores genéricos del servidor)
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception ex, Model model) {
        model.addAttribute("error", "Ups! Ha ocurrido un error interno.");
        model.addAttribute("mensaje", ex.getMessage()); // En producción, mejor no mostrar el mensaje exacto
        return "error/500"; // Redirige a templates/error/500.html
    }

    // 2. Capturar errores 404 (Página no encontrada)
    // Nota: Requiere una propiedad en application.properties para funcionar
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handle404(NoHandlerFoundException ex, Model model) {
        model.addAttribute("titulo", "Página no encontrada");
        return "error/404";
    }

    // 3. Capturar errores de lógica de negocio (Ej: PIN inválido)
    @ExceptionHandler(RuntimeException.class)
    public String handleRuntime(RuntimeException ex, Model model) {
        model.addAttribute("error", "Error en la operación");
        model.addAttribute("mensaje", ex.getMessage());
        return "error/error-app";
    }
}
