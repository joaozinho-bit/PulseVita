package com.pulsevita.pulsevita.repository;

import com.pulsevita.pulsevita.model.Dispositivo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DispositivoRepository extends JpaRepository<Dispositivo, Integer> {
    Optional<Dispositivo> findByIdDispositivo(String idDispositivo);
}