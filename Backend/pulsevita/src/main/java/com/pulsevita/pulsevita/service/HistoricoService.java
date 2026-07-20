package com.pulsevita.pulsevita.service;

import com.pulsevita.pulsevita.controller.HistoricoDTO;
import com.pulsevita.pulsevita.model.HistoricoPaciente;
import com.pulsevita.pulsevita.repository.HistoricoPacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HistoricoService {

    @Autowired
    private HistoricoPacienteRepository repository;

    public List<HistoricoDTO> listarHistorico(Long idPaciente, String tipo, LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicio = dataInicio != null ? dataInicio.atStartOfDay() : null;
        LocalDateTime fim = dataFim != null ? dataFim.atTime(LocalTime.MAX) : null;

        String tipoNormalizado = tipo == null ? "AMBOS" : tipo.trim().toUpperCase();

        List<HistoricoPaciente> registos = repository.buscarHistorico(idPaciente, inicio, fim);

        return registos.stream().map(h -> {
            HistoricoDTO dto = new HistoricoDTO();
            dto.id = h.getId();
            dto.dataLeitura = h.getDataLeitura().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

            if (tipoNormalizado.equals("BPM") || tipoNormalizado.equals("AMBOS")) {
                dto.bpm = h.getBpm();
            }
            if (tipoNormalizado.equals("TEMPERATURA") || tipoNormalizado.equals("AMBOS")) {
                dto.temperatura = h.getTemperatura() != null ? h.getTemperatura().doubleValue() : null;
            }

            return dto;
        }).collect(Collectors.toList());
    }
}