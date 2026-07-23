package com.pulsevita.pulsevita.repository;

import com.pulsevita.pulsevita.model.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    // consultas confirmadas de um paciente, ordenadas por data/hora
    List<Consulta> findByPacienteIdAndEstadoOrderByDataConsultaAscHoraConsultaAsc(Long idPaciente, String estado);

    // todas as consultas de um paciente, para o ecra de marcacoes com filtros por estado
    List<Consulta> findByPacienteIdOrderByDataConsultaDescHoraConsultaDesc(Long idPaciente);

     List<Consulta> findByDataConsultaBetweenOrderByDataConsultaAscHoraConsultaAsc(LocalDate inicio, LocalDate fim);

    // Usado para recusar automaticamente os outros pedidos do mesmo horário
    List<Consulta> findByDataConsultaAndHoraConsultaAndEstado(LocalDate data, LocalTime hora, String estado);

    List<Consulta> findByDataConsultaAndEstado(LocalDate data, String estado);
}