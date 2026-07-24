package com.pulsevita.pulsevita.repository;

import com.pulsevita.pulsevita.model.HistoricoPaciente;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoricoPacienteRepository extends JpaRepository<HistoricoPaciente, Long> {

    // datas nulas ignoram o filtro; a direcao da ordenacao vem no Sort,
    // para a mesma query servir "mais recentes" e "mais antigas"
    @Query("SELECT h FROM HistoricoPaciente h WHERE h.idPaciente = :idPaciente " +
           "AND (CAST(:inicio AS timestamp) IS NULL OR h.dataLeitura >= :inicio) " +
           "AND (CAST(:fim AS timestamp) IS NULL OR h.dataLeitura <= :fim)")
    List<HistoricoPaciente> buscarHistorico(
            @Param("idPaciente") Long idPaciente,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            Sort sort
    );

    // ultima medicao do paciente (a mais recente); serve o dashboard, tanto para
    // o resumo "ultima medicao" como para saber se ja houve medicao hoje
    HistoricoPaciente findTopByIdPacienteOrderByDataLeituraDesc(Long idPaciente);
}
