package com.example.demo.controller.api;

import com.example.demo.model.Empleado;
import com.example.demo.repository.EmpleadoRepository;
import com.example.demo.repository.PermisoRepository;
import com.example.demo.repository.VacacionRepository;
import com.example.demo.service.EmpleadoService;
import com.example.demo.service.VacacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/perfil")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PerfilRestController {

    private final EmpleadoService empleadoService;
    private final EmpleadoRepository empleadoRepository;
    private final VacacionRepository vacacionRepository;
    private final VacacionService vacacionService;
    private final PermisoRepository permisoRepository;

    // Obtener los datos actuales del perfil del empleado
    @GetMapping("/{id}")
    public Map<String, Object> obtenerPerfil(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        Empleado empleado = empleadoService.obtenerPorId(id).orElse(null);
        if (empleado == null) {
            response.put("success", false);
            response.put("message", "Empleado no encontrado");
            return response;
        }

        vacacionService.verificarRegresoVacaciones(empleado);

        LocalDate hoy = LocalDate.now();
        boolean estaDeVacaciones = vacacionRepository.existsVacacionActiva(empleado, hoy);
        boolean tienePermisoHoy = permisoRepository.existsPermisoActivo(empleado, hoy);

        response.put("success", true);
        response.put("empleado", empleado);
        response.put("enVacaciones", estaDeVacaciones);
        response.put("conPermiso", tienePermisoHoy);

        return response;
    }

    // Guardar cambios del perfil desde Flutter (Autoservicio)
    @PutMapping("/editar/{id}")
    public Map<String, Object> actualizarPerfil(@PathVariable Long id, @RequestBody Empleado datosModificados) {
        Map<String, Object> response = new HashMap<>();
        try {
            Empleado empleadoBD = empleadoService.obtenerPorId(id)
                    .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

            // Actualizamos campos de autoservicio autorizados
            empleadoBD.setEmail(datosModificados.getEmail());
            empleadoBD.setTelefono(datosModificados.getTelefono());
            empleadoBD.setDireccion(datosModificados.getDireccion());
            empleadoBD.setFechaNacimiento(datosModificados.getFechaNacimiento());

            empleadoService.actualizar(id, empleadoBD);

            response.put("success", true);
            response.put("empleado", empleadoBD); // Devolvemos el empleado actualizado
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
}
