package com.example.demo.controller;

import com.example.demo.model.Documento;
import com.example.demo.model.Empleado;
import com.example.demo.model.PQRS;
import com.example.demo.repository.DocumentoRepository;
import com.example.demo.repository.PQRSRepository;
import com.example.demo.service.FileStorageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

@Controller
@RequestMapping("/empleado")
public class EmpleadoGestionController {

    @Autowired
    private FileStorageService fileService;

    @Autowired
    private DocumentoRepository docRepo;

    @Autowired
    private PQRSRepository pqrsRepo;

    // SUBIR DOCUMENTO
    @PostMapping("/documentos/subir")
    public String subirDocumento(@RequestParam("archivo") MultipartFile archivo,
                                 @RequestParam("tipo") String tipo,
                                 HttpSession session) throws IOException {
        Empleado emp = (Empleado) session.getAttribute("empleado");

        String nombreArchivo = fileService.guardarArchivo(archivo, emp.getDocumento());

        Documento doc = new Documento();
        doc.setEmpleado(emp);
        doc.setNombre(nombreArchivo);
        doc.setTipo(tipo);
        doc.setFechaSubida(LocalDate.now());
        docRepo.save(doc);

        return "redirect:/empleado/perfil";
    }

    // CREAR PQRS
    @PostMapping("/pqrs/crear")
    public String crearPQRS(@ModelAttribute PQRS pqrs, HttpSession session) {
        Empleado emp = (Empleado) session.getAttribute("empleado");
        pqrs.setEmpleado(emp);
        pqrs.setFechaCreacion(LocalDate.now());
        pqrs.setEstado("PENDIENTE");
        pqrsRepo.save(pqrs);
        return "redirect:/empleado/perfil";
    }

    @PostMapping("/admin/pqrs/responder")
    public String responderPQRS(@RequestParam Long id, @RequestParam String respuesta) {
        PQRS pqrs = pqrsRepo.findById(id).orElseThrow();
        pqrs.setRespuestaRRHH(respuesta);
        pqrs.setEstado("RESUELTO"); // Cambia el estado automáticamente
        pqrsRepo.save(pqrs);
        return "redirect:/empleado/admin/gestion-pqrs";
    }

    @GetMapping("/perfil")
    public String verMiPerfil(HttpSession session, Model model) {
        Empleado emp = (Empleado) session.getAttribute("empleado");
        if (emp == null) return "redirect:/login";

        // Cargamos los datos específicos de este empleado
        model.addAttribute("misDocumentos", docRepo.findByEmpleado(emp));
        model.addAttribute("misPQRS", pqrsRepo.findByEmpleadoOrderByFechaCreacionDesc(emp));
        model.addAttribute("empleado", emp);

        return "mi-perfil";
    }

    @GetMapping("/admin/gestion-pqrs")
    public String gestionarPQRS(Model model, HttpSession session) {
        Empleado usuario = (Empleado) session.getAttribute("empleado");

        // Seguridad: Solo ADMIN o RRHH
        if (usuario == null || (!usuario.getRol().toString().equals("ADMIN") && !usuario.getRol().toString().equals("RRHH"))) {
            return "redirect:/index";
        }

        // 1. Obtenemos todos los PQRS
        model.addAttribute("todosLosPQRS", pqrsRepo.findAll());

        // 2. Obtenemos TODOS los documentos subidos por cualquier empleado
        // Esto es lo que permite a RRHH ver los archivos de todos
        model.addAttribute("todosLosDocumentos", docRepo.findAll());

        return "gestion-pqrs";
    }

    @PostMapping("/documentos/eliminar/{id}")
    public String eliminarDocumento(@PathVariable Long id, HttpSession session) {
        Documento doc = docRepo.findById(id).orElseThrow();
        // 1. Borrar archivo físico
        File file = new File("uploads/" + doc.getNombre());
        if(file.exists()) file.delete();

        // 2. Borrar registro en DB
        docRepo.delete(doc);
        return "redirect:/empleado/perfil";
    }
}
