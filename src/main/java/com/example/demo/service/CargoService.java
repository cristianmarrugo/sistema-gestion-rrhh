package com.example.demo.service;

import com.example.demo.model.Cargo;
import com.example.demo.repository.CargoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CargoService {

    private final CargoRepository cargoRepository;

    public List<Cargo> listarTodos() {
        return cargoRepository.findAll();
    }

    public Optional<Cargo> obtenerPorId(Long id) {
        return cargoRepository.findById(id);
    }

    @Transactional
    public Cargo crear(Cargo cargo) {
        return cargoRepository.save(cargo);
    }

    @Transactional
    public Optional<Cargo> actualizar(Long id, Cargo cargoActualizado) {
        return cargoRepository.findById(id)
                .map(cargo -> {
                    cargo.setNombre(cargoActualizado.getNombre());
                    cargo.setSalarioBase(cargoActualizado.getSalarioBase());
                    return cargoRepository.save(cargo);
                });
    }

    @Transactional
    public boolean eliminar(Long id) {
        return cargoRepository.findById(id)
                .map(cargo -> {
                    cargoRepository.delete(cargo);
                    return true;
                })
                .orElse(false);
    }
}
