package com.example.demo.controller;

import com.example.demo.model.Documento;
import com.example.demo.model.Empleado;
import com.example.demo.model.PQRS;
import com.example.demo.model.Rol;
import com.example.demo.repository.DocumentoRepository;
import com.example.demo.repository.EmpleadoRepository;
import com.example.demo.repository.PQRSRepository;
import com.example.demo.service.FileStorageService;
import com.example.demo.service.NotificacionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/empleado")
public class EmpleadoGestionController {

    @Autowired
    private FileStorageService fileService;

    @Autowired
    private DocumentoRepository docRepo;

    @Autowired
    private PQRSRepository pqrsRepo;

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private EmpleadoRepository empleadoRepository;

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
        // 1. Obtenemos el empleado de la sesión (usamos la variable 'emp')
        Empleado emp = (Empleado) session.getAttribute("empleado");

        if (emp == null) {
            return "redirect:/pin";
        }

        // 2. Seteamos los valores básicos de la solicitud
        pqrs.setEmpleado(emp);
        pqrs.setFechaCreacion(LocalDate.now());
        pqrs.setEstado("PENDIENTE");

        // 3. Buscamos a los encargados de recibir la notificación
        List<Empleado> rrhh = empleadoRepository.findByRol(Rol.RRHH);

        // 4. Guardamos la PQRS en la base de datos
        pqrsRepo.save(pqrs);

        // 5. Ciclo para generar notificaciones a cada administrativo
        for (Empleado admin : rrhh) {
            notificacionService.crear(
                    admin,
                    "📩 Nueva " + pqrs.getTipo() + " radicada por " + emp.getNombre(), // Corregido: 'emp' en lugar de 'autor'
                    "/empleado/admin/gestion-pqrs"
            );


        }

        return "redirect:/empleado/perfil";
    }

    @PostMapping("/admin/pqrs/responder")
    public String responderPQRS(@RequestParam Long id, @RequestParam String respuesta, HttpSession session) {

        Empleado admin = (Empleado) session.getAttribute("empleado");

        PQRS pqrs = pqrsRepo.findById(id).orElseThrow();
        pqrs.setRespuestaRRHH(respuesta);

        if (!admin.getRol().toString().equals("RRHH") &&
                !admin.getRol().toString().equals("ADMIN")) {
            throw new RuntimeException("No autorizado");
        }
        pqrs.setEstado("RESUELTO");// Cambia el estado automáticamente

        pqrsRepo.save(pqrs);
        // 🔔 NOTIFICACIÓN AL EMPLEADO
        notificacionService.crear(
                pqrs.getEmpleado(),
                "📬 Tu PQRS ha sido respondida",
                "/empleado/perfil"
        );
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
