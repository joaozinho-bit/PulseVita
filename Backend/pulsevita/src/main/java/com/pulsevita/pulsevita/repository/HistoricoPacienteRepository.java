package com.pulsevita.pulsevita.repository;

import com.pulsevita.pulsevita.model.HistoricoPaciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoricoPacienteRepository extends JpaRepository<HistoricoPaciente, Long> {

    @Query("SELECT h FROM HistoricoPaciente h WHERE h.paciente.id = :idPaciente " +
           "AND (CAST(:inicio AS timestamp) IS NULL OR h.dataLeitura >= :inicio) " +
           "AND (CAST(:fim AS timestamp) IS NULL OR h.dataLeitura <= :fim) " +
           "ORDER BY h.dataLeitura ASC")
    List<HistoricoPaciente> buscarHistorico(
            @Param("idPaciente") Long idPaciente,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );
}