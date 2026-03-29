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

        if (empleadoRepository.findByDocumento(empleado.getDocumento()).isPresent()) {
            throw new RuntimeException("El documento ya está registrado.");
        }
        if (empleadoRepository.findByEmail(empleado.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado.");
        }
        // Generar PIN automático si no viene
        if (empleado.getPin() == null || empleado.getPin().isEmpty()) {
            empleado.setPin(generarPinUnico());
        }else {
            if (empleadoRepository.findByPin(empleado.getPin()).isPresent()) {
                throw new RuntimeException("El PIN ya está en uso por otro empleado.");
            }
        }

        // Por defecto activo
        empleado.setActivo(true);

        return empleadoRepository.save(empleado);
    }

    @Transactional
    public Optional<Empleado> actualizar(Long id, Empleado empleadoActualizado) {
        return empleadoRepository.findById(id)
                .map(empleado -> {

                    // 1. Validar Email Duplicado (Si cambió el email)
                    if (!empleado.getEmail().equals(empleadoActualizado.getEmail())) {
                        if (empleadoRepository.findByEmail(empleadoActualizado.getEmail()).isPresent()) {
                            throw new RuntimeException("El email ya está registrado por otro empleado.");
                        }
                    }

                    // 2. Validar Documento Duplicado (Si cambió el documento)
                    if (!empleado.getDocumento().equals(empleadoActualizado.getDocumento())) {
                        if (empleadoRepository.findByDocumento(empleadoActualizado.getDocumento()).isPresent()) {
                            throw new RuntimeException("El documento ya está registrado por otro empleado.");
                        }
                    }

                    // 3. Validar PIN Duplicado (Si se envió un PIN nuevo)
                    if (empleadoActualizado.getPin() != null && !empleadoActualizado.getPin().isEmpty()) {
                        if (!empleado.getPin().equals(empleadoActualizado.getPin())) {
                            if (empleadoRepository.findByPin(empleadoActualizado.getPin()).isPresent()) {
                                throw new RuntimeException("El PIN ya está en uso por otro empleado.");
                            }
                            empleado.setPin(empleadoActualizado.getPin());
                        }
                    }
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
