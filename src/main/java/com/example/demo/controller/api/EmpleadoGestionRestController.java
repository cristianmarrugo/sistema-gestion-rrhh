package com.example.demo.controller.api;

import com.example.demo.model.Documento;
import com.example.demo.model.Empleado;
import com.example.demo.model.PQRS;
import com.example.demo.model.Rol;
import com.example.demo.repository.DocumentoRepository;
import com.example.demo.repository.EmpleadoRepository;
import com.example.demo.repository.PQRSRepository;
import com.example.demo.service.FileStorageService;
import com.example.demo.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/empleado")
@CrossOrigin(origins = "*")
public class EmpleadoGestionRestController {

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

    // 1. OBTENER PERFIL COMPLETO DESDE MÓVIL
    @GetMapping("/perfil/{id}")
    public Map<String, Object> verMiPerfil(@PathVariable Long id) {
        Empleado emp = empleadoRepository.findById(id).orElseThrow();

        Map<String, Object> response = new HashMap<>();
        response.put("empleado", emp);
        response.put("misDocumentos", docRepo.findByEmpleado(emp));
        response.put("misPQRS", pqrsRepo.findByEmpleadoOrderByFechaCreacionDesc(emp));
        return response;
    }

    // 2. CREAR PQRS DESDE EL CELULAR
    @PostMapping("/pqrs/crear")
    public Map<String, Object> crearPQRS(@RequestBody PQRS pqrs, @RequestParam Long empleadoId) {
        Empleado emp = empleadoRepository.findById(empleadoId).orElseThrow();

        pqrs.setEmpleado(emp);
        pqrs.setFechaCreacion(LocalDate.now());
        pqrs.setEstado("PENDIENTE");

        pqrsRepo.save(pqrs);

        List<Empleado> rrhh = empleadoRepository.findByRol(Rol.RRHH);
        for (Empleado admin : rrhh) {
            notificacionService.crear(admin, "📩 Nueva " + pqrs.getTipo() + " radicada por " + emp.getNombre(), "/empleado/admin/gestion-pqrs");
        }

        return Map.of("success", true, "message", "PQRS enviada con éxito");
    }

    // 3. SUBIR ARCHIVO DESDE FLUTTER
    @PostMapping("/documentos/subir")
    public Map<String, Object> subirDocumento(@RequestParam("archivo") MultipartFile archivo,
                                              @RequestParam("tipo") String tipo,
                                              @RequestParam Long empleadoId) {
        try {
            Empleado emp = empleadoRepository.findById(empleadoId).orElseThrow();
            String nombreArchivo = fileService.guardarArchivo(archivo, emp.getDocumento());

            Documento doc = new Documento();
            doc.setEmpleado(emp);
            doc.setNombre(nombreArchivo);
            doc.setTipo(tipo);
            doc.setFechaSubida(LocalDate.now());
            docRepo.save(doc);

            return Map.of("success", true, "nombreArchivo", nombreArchivo);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    // 4. RESPONDER PQRS (MÓVIL ADMINISTRATIVO)
    @PostMapping("/admin/pqrs/responder")
    public Map<String, Object> responderPQRS(@RequestParam Long id, @RequestParam String respuesta, @RequestParam Long adminId) {
        Empleado admin = empleadoRepository.findById(adminId).orElseThrow();

        if (!admin.getRol().toString().equals("RRHH") && !admin.getRol().toString().equals("ADMIN")) {
            return Map.of("success", false, "message", "No autorizado");
        }

        PQRS pqrs = pqrsRepo.findById(id).orElseThrow();
        pqrs.setRespuestaRRHH(respuesta);
        pqrs.setEstado("RESUELTO");
        pqrsRepo.save(pqrs);

        notificacionService.crear(pqrs.getEmpleado(), "📬 Tu PQRS ha sido respondida", "/empleado/perfil");
        return Map.of("success", true);
    }

    // 5. GESTIÓN GLOBAL PARA RRHH/ADMIN DESDE EL CELULAR
    @GetMapping("/admin/gestion-global")
    public Map<String, Object> gestionarPQRS(@RequestParam Long adminId) {
        Empleado usuario = empleadoRepository.findById(adminId).orElseThrow();

        if (!usuario.getRol().toString().equals("ADMIN") && !usuario.getRol().toString().equals("RRHH")) {
            return Map.of("error", "No autorizado");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("todosLosPQRS", pqrsRepo.findAll());
        response.put("todosLosDocumentos", docRepo.findAll());
        return response;
    }

    // 6. ELIMINAR DOCUMENTO
    @DeleteMapping("/documentos/eliminar/{id}")
    public Map<String, Object> eliminarDocumento(@PathVariable Long id) {
        try {
            Documento doc = docRepo.findById(id).orElseThrow();
            File file = new File("uploads/" + doc.getNombre());
            if (file.exists()) file.delete();

            docRepo.delete(doc);
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
}
