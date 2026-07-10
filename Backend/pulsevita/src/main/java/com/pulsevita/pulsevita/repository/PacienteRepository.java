package com.pulsevita.pulsevita.repository;

import com.pulsevita.pulsevita.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    Paciente findByEmail(String email);

    @Query("SELECT p FROM Paciente p WHERE p.n_paciente = :numero")
    Paciente findByNumeroPaciente(@Param("numero") String numero);

    // Verifica se outro paciente já tem este dispositivo associado
    boolean existsByDispositivoId(Integer idDispositivo);
}