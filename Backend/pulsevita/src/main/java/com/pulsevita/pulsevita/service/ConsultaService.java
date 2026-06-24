package com.pulsevita.pulsevita.service;

import com.pulsevita.pulsevita.model.Consulta;
import com.pulsevita.pulsevita.model.Utilizador;
import com.pulsevita.pulsevita.repository.ConsultaRepository;
import com.pulsevita.pulsevita.repository.UtilizadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class ConsultaService {

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private UtilizadorRepository utilizadorRepository;

    // Cria uma marcação com estado 
    public Consulta marcarConsulta(Long idUtilizador, LocalDate data, LocalTime hora) {
        Utilizador utilizador = utilizadorRepository.findById(idUtilizador).orElse(null);
        if (utilizador == null) return null;

        Consulta consulta = new Consulta();
        consulta.setUtilizador(utilizador);
        consulta.setDataConsulta(data);
        consulta.setHoraConsulta(hora);
        consulta.setEstado("POR_CONFIRMAR");

        return consultaRepository.save(consulta);
    }

    // Todas as consultas confirmadas
    public List<Consulta> listarConsultasConfirmadas(Long idUtilizador) {
        return consultaRepository.findByUtilizadorIdAndEstadoOrderByDataConsultaAscHoraConsultaAsc(idUtilizador, "CONFIRMADA");
    }

    // Apenas a próxima consulta confirmada 
    public Optional<Consulta> proximaConsulta(Long idUtilizador) {
        return listarConsultasConfirmadas(idUtilizador).stream()
                .filter(c -> !c.getDataConsulta().isBefore(LocalDate.now()))
                .findFirst();
    }
}