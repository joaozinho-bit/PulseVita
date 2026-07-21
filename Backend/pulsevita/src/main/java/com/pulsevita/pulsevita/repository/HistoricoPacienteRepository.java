package com.pulsevita.pulsevita.repository;

import com.pulsevita.pulsevita.model.HistoricoPaciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricoPacienteRepository extends JpaRepository<HistoricoPaciente, Long> {

    List<HistoricoPaciente> findByIdPacienteOrderByDataLeituraDesc(Long idPaciente);
}
