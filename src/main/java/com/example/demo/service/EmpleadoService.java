package com.example.demo.service;

import com.example.demo.model.Empleado;
import com.example.demo.repository.EmpleadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;

    public List<Empleado> listarTodos() {
        return empleadoRepository.findAll();
    }

    public List<Empleado> listarActivos() {
        return empleadoRepository.findAll()
                .stream()
                .filter(Empleado::isActivo)
                .toList();
    }

    public Optional<Empleado> obtenerPorId(Long id) {
        return empleadoRepository.findById(id);
    }

    public Empleado buscarPorDocumento(String documento) {
        return empleadoRepository.findByDocumento(documento)
                .orElse(null);
    }

    public List<Empleado> buscar(String query) {
        String q = query.toLowerCase();
        return empleadoRepository.findAll()
                .stream()
                .filter(e -> e.getNombre().toLowerCase().contains(q) ||
                        e.getApellido().toLowerCase().contains(q) ||
                        e.getDocumento().contains(q) ||
                        e.getEmail().toLowerCase().contains(q))
                .toList();
    }

    @Transactional
    public Empleado crear(Empleado empleado) {
        // Generar PIN automático si no viene
        if (empleado.getPin() == null || empleado.getPin().isEmpty()) {
            empleado.setPin(generarPinUnico());
        }

        // Por defecto activo
        empleado.setActivo(true);

        return empleadoRepository.save(empleado);
    }

    @Transactional
    public Optional<Empleado> actualizar(Long id, Empleado empleadoActualizado) {
        return empleadoRepository.findById(id)
                .map(empleado -> {
                    empleado.setNombre(empleadoActualizado.getNombre());
                    empleado.setApellido(empleadoActualizado.getApellido());
                    empleado.setDocumento(empleadoActualizado.getDocumento());
                    empleado.setEmail(empleadoActualizado.getEmail());
                    empleado.setRol(empleadoActualizado.getRol());
                    empleado.setFechaIngreso(empleadoActualizado.getFechaIngreso());
                    empleado.setCargo(empleadoActualizado.getCargo());

                    // Horario puede ser null (opcional)
                    empleado.setHorario(empleadoActualizado.getHorario());

                    // ✅ ACTUALIZAR PIN Y ACTIVO
                    if (empleadoActualizado.getPin() != null && !empleadoActualizado.getPin().isEmpty()) {
                        empleado.setPin(empleadoActualizado.getPin());
                    }
                    empleado.setActivo(empleadoActualizado.isActivo());

                    return empleadoRepository.save(empleado);
                });
    }

    @Transactional
    public boolean desactivar(Long id) {
        return empleadoRepository.findById(id)
                .map(empleado -> {
                    empleado.setActivo(false);
                    empleadoRepository.save(empleado);
                    return true;
                })
                .orElse(false);
    }

    // Generar PIN único de 4 dígitos
    private String generarPinUnico() {
        SecureRandom random = new SecureRandom();
        String pin;

        do {
            pin = String.format("%04d", random.nextInt(1000000));
        } while (empleadoRepository.findByPin(pin).isPresent());

        return pin;
    }
}
