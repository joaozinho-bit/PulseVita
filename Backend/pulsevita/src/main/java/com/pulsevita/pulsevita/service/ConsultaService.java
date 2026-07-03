package com.pulsevita.pulsevita.service;

import com.pulsevita.pulsevita.model.Consulta;
import com.pulsevita.pulsevita.model.Paciente;
import com.pulsevita.pulsevita.repository.ConsultaRepository;
import com.pulsevita.pulsevita.repository.PacienteRepository;
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
    private PacienteRepository pacienteRepository;

    // Cria uma marcação com estado 
    public Consulta marcarConsulta(Long idPaciente, LocalDate data, LocalTime hora) {
        Paciente paciente = pacienteRepository.findById(idPaciente).orElse(null);
        if (paciente == null) return null;

        Consulta consulta = new Consulta();
        consulta.setPaciente(paciente);
        consulta.setDataConsulta(data);
        consulta.setHoraConsulta(hora);
        consulta.setEstado("POR_CONFIRMAR");

        return consultaRepository.save(consulta);
    }

    // Todas as consultas confirmadas
    public List<Consulta> listarConsultasConfirmadas(Long idPaciente) {
        return consultaRepository.findByPacienteIdAndEstadoOrderByDataConsultaAscHoraConsultaAsc(idPaciente, "CONFIRMADA");
    }

    // Apenas a próxima consulta confirmada 
    public Optional<Consulta> proximaConsulta(Long idPaciente) {
        return listarConsultasConfirmadas(idPaciente).stream()
                .filter(c -> !c.getDataConsulta().isBefore(LocalDate.now()))
                .findFirst();
    }
}