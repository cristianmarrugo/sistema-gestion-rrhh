package com.example.demo.service;


import com.example.demo.model.Horario;
import com.example.demo.repository.HorarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HorarioService {

    private final HorarioRepository horarioRepository;

    public List<Horario> listarTodos() {
        return horarioRepository.findAll();
    }

    public Optional<Horario> obtenerPorId(Long id) {
        return horarioRepository.findById(id);
    }

    @Transactional
    public Horario crear(Horario horario) {
        return horarioRepository.save(horario);
    }

    @Transactional
    public Optional<Horario> actualizar(Long id, Horario horarioActualizado) {
        return horarioRepository.findById(id)
                .map(horario -> {
                    horario.setHoraEntrada(horarioActualizado.getHoraEntrada());
                    horario.setHoraSalida(horarioActualizado.getHoraSalida());
                    horario.setToleranciaMinutos(horarioActualizado.getToleranciaMinutos());
                    return horarioRepository.save(horario);
                });
    }

    @Transactional
    public boolean eliminar(Long id) {
        return horarioRepository.findById(id)
                .map(horario -> {
                    horarioRepository.delete(horario);
                    return true;
                })
                .orElse(false);
    }
}
