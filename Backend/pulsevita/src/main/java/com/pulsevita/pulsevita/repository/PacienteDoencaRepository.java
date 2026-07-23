package com.pulsevita.pulsevita.repository;

import com.pulsevita.pulsevita.model.PacienteDoenca;
import com.pulsevita.pulsevita.model.PacienteDoencaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PacienteDoencaRepository extends JpaRepository<PacienteDoenca, PacienteDoencaId> {

    List<PacienteDoenca> findByIdPaciente(Long idPaciente);
}
