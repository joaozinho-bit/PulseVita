package com.pulsevita.pulsevita.repository;

import com.pulsevita.pulsevita.model.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    // consultas confirmadas de um utilizador, ordenadas por data/hora
    List<Consulta> findByUtilizadorIdAndEstadoOrderByDataConsultaAscHoraConsultaAsc(Long idUtilizador, String estado);

    // Todas as consultas de um mês, para o calendário do médico
    List<Consulta> findByDataConsultaBetweenOrderByDataConsultaAscHoraConsultaAsc(LocalDate inicio, LocalDate fim);

    // Usado para recusar automaticamente os outros pedidos do mesmo horário
    List<Consulta> findByDataConsultaAndHoraConsultaAndEstado(LocalDate data, LocalTime hora, String estado);

    List<Consulta> findByDataConsultaAndEstado(LocalDate data, String estado);
}