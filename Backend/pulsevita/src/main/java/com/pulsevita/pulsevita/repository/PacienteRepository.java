package com.pulsevita.pulsevita.repository;

import com.pulsevita.pulsevita.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    Paciente findByEmail(String email); //busca paciente por email
}