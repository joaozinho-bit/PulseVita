package com.pulsevita.pulsevita.service;

import com.pulsevita.pulsevita.model.Consulta;
import com.pulsevita.pulsevita.model.Medico;
import com.pulsevita.pulsevita.model.Utilizador;
import com.pulsevita.pulsevita.repository.ConsultaRepository;
import com.pulsevita.pulsevita.repository.MedicoRepository;
import com.pulsevita.pulsevita.repository.UtilizadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ConsultaService {

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private UtilizadorRepository utilizadorRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    public Consulta marcarConsulta(Long idUtilizador, LocalDate data, LocalTime hora, String motivo) {
        Utilizador utilizador = utilizadorRepository.findById(idUtilizador).orElse(null);
        if (utilizador == null) return null;

        Consulta consulta = new Consulta();
        consulta.setUtilizador(utilizador);
        consulta.setDataConsulta(data);
        consulta.setHoraConsulta(hora);
        consulta.setEstado("POR_CONFIRMAR");
        consulta.setMotivo(motivo);

        return consultaRepository.save(consulta);
    }

    public List<Consulta> listarConsultasConfirmadas(Long idUtilizador) {
        return consultaRepository.findByUtilizadorIdAndEstadoOrderByDataConsultaAscHoraConsultaAsc(idUtilizador, "CONFIRMADA");
    }

    public Optional<Consulta> proximaConsulta(Long idUtilizador) {
        return listarConsultasConfirmadas(idUtilizador).stream()
                .filter(c -> !c.getDataConsulta().isBefore(LocalDate.now()))
                .findFirst();
    }

    // ---- Calendário do médico ----

    public List<Consulta> listarConsultasDoMes(int ano, int mes) {
        LocalDate inicio = LocalDate.of(ano, mes, 1);
        LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());
        return consultaRepository.findByDataConsultaBetweenOrderByDataConsultaAscHoraConsultaAsc(inicio, fim);
    }

    // Confirma uma consulta e recusa automaticamente as restantes no mesmo dia+hora
    public Consulta confirmarConsulta(Long idConsulta, Long idMedico) {
        Consulta consulta = consultaRepository.findById(idConsulta).orElse(null);
        if (consulta == null) return null;

        consulta.setEstado("CONFIRMADA");
        if (idMedico != null) {
            Medico medico = medicoRepository.findById(idMedico).orElse(null);
            consulta.setMedico(medico);
        }
        consultaRepository.save(consulta);

        int horaBloco = consulta.getHoraConsulta().getHour();

        List<Consulta> pendentesDoDia = consultaRepository.findByDataConsultaAndEstado(
                consulta.getDataConsulta(), "POR_CONFIRMAR");

        for (Consulta outra : pendentesDoDia) {
            boolean mesmoBloco = outra.getHoraConsulta().getHour() == horaBloco;
            if (mesmoBloco && !outra.getId().equals(idConsulta)) {
                outra.setEstado("CANCELADA");
                consultaRepository.save(outra);
            }
        }

        return consulta;
    }

    public Consulta recusarConsulta(Long idConsulta) {
        Consulta consulta = consultaRepository.findById(idConsulta).orElse(null);
        if (consulta == null) return null;
        consulta.setEstado("CANCELADA");
        return consultaRepository.save(consulta);
    }
}