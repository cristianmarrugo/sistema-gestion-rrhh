package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 1. Errores de lógica de negocio (PIN, falta de permisos, etc.)
    // Ponemos este arriba para que sea más específico
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleRuntime(RuntimeException ex, Model model) {
        model.addAttribute("titulo", "Error en la operación");
        model.addAttribute("mensaje", ex.getMessage());
        return "error/error-app";
    }

    // 2. Errores 404 (Página no encontrada)
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handle404(NoHandlerFoundException ex, Model model) {
        model.addAttribute("titulo", "Página no encontrada");
        return "error/404";
    }

    // 3. El "Catcher" final para errores 500 (Errores inesperados de mapeo, DB, etc.)
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception ex, Model model) {
        ex.printStackTrace(); // Vital para que veas el error en IntelliJ
        model.addAttribute("error", "Ha ocurrido un error inesperado");
        model.addAttribute("mensaje", "Detalle: " + ex.getClass().getSimpleName());
        return "error/500";
    }
}
