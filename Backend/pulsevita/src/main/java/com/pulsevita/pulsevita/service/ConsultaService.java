package com.pulsevita.pulsevita.service;

import com.pulsevita.pulsevita.model.Consulta;
import com.pulsevita.pulsevita.model.Medico;
import com.pulsevita.pulsevita.model.Paciente;
import com.pulsevita.pulsevita.repository.ConsultaRepository;
import com.pulsevita.pulsevita.repository.MedicoRepository;
import com.pulsevita.pulsevita.repository.PacienteRepository;
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
    private PacienteRepository pacienteRepository;
 @Autowired
    private MedicoRepository medicoRepository;
    // Cria uma marcação com estado 
    public Consulta marcarConsulta(Long idPaciente, LocalDate data, LocalTime hora, String motivo) {
        Paciente paciente = pacienteRepository.findById(idPaciente).orElse(null);
        if (paciente == null) return null;

        Consulta consulta = new Consulta();
        consulta.setPaciente(paciente);
        consulta.setDataConsulta(data);
        consulta.setHoraConsulta(hora);
        consulta.setEstado("POR_CONFIRMAR");
        consulta.setMotivo(motivo);

        return consultaRepository.save(consulta);
    }

    // Todas as consultas confirmadas
    public List<Consulta> listarConsultasConfirmadas(Long idPaciente) {
        return consultaRepository.findByPacienteIdAndEstadoOrderByDataConsultaAscHoraConsultaAsc(idPaciente, "CONFIRMADA");
    }

    // Todas as consultas do paciente (qualquer estado), da mais recente para a mais antiga
    public List<Consulta> listarConsultasDoPaciente(Long idPaciente) {
        return consultaRepository.findByPacienteIdOrderByDataConsultaDescHoraConsultaDesc(idPaciente);
    }

    // Apenas a próxima consulta confirmada 
    public Optional<Consulta> proximaConsulta(Long idPaciente) {
        return listarConsultasConfirmadas(idPaciente).stream()
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