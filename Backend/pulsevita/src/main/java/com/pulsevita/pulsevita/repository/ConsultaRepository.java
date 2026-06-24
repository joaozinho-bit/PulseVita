package com.pulsevita.pulsevita.repository;

import com.pulsevita.pulsevita.model.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    // consultas confirmadas de um utilizador, ordenadas por data/hora
    List<Consulta> findByUtilizadorIdAndEstadoOrderByDataConsultaAscHoraConsultaAsc(Long idUtilizador, String estado);
}