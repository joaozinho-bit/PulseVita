package com.pulsevita.pulsevita.repository;

import com.pulsevita.pulsevita.model.ValoresReferenciaPaciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ValoresReferenciaPacienteRepository extends JpaRepository<ValoresReferenciaPaciente, Long> {

    // o registo mais recente e o que esta em vigor para o paciente
    ValoresReferenciaPaciente findTopByIdPacienteOrderByDataDefinicaoDesc(Long idPaciente);
}
