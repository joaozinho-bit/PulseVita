package com.pulsevita.pulsevita.service;

import com.pulsevita.pulsevita.controller.HistoricoDTO;
import com.pulsevita.pulsevita.model.HistoricoPaciente;
import com.pulsevita.pulsevita.model.TipoMedicao;
import com.pulsevita.pulsevita.repository.HistoricoPacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    public List<HistoricoDTO> listarHistorico(Long idPaciente, String tipo, LocalDate dataInicio,
                                              LocalDate dataFim, String ordem) {
        LocalDateTime inicio = dataInicio != null ? dataInicio.atStartOfDay() : null;
        LocalDateTime fim = dataFim != null ? dataFim.atTime(LocalTime.MAX) : null;
        TipoMedicao tipoFiltro = interpretarTipo(tipo);

        Sort sort = "antigas".equalsIgnoreCase(ordem)
                ? Sort.by(Sort.Direction.ASC, "dataLeitura")
                : Sort.by(Sort.Direction.DESC, "dataLeitura");

        List<HistoricoPaciente> registos = repository.buscarHistorico(idPaciente, inicio, fim, sort);

        return registos.stream()
                .filter(h -> tipoFiltro == null || h.getTipoMedicao() == tipoFiltro)
                .map(this::paraDto)
                .collect(Collectors.toList());
    }

    // sem tipo devolve todos os registos; um tipo concreto filtra pelo
    // campo tipo_medicao guardado em cada medicao
    private TipoMedicao interpretarTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return null;
        }
        try {
            return TipoMedicao.valueOf(tipo.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de medição inválido.");
        }
    }

    private HistoricoDTO paraDto(HistoricoPaciente h) {
        HistoricoDTO dto = new HistoricoDTO();
        dto.id = h.getId();
        dto.tipoMedicao = h.getTipoMedicao() != null ? h.getTipoMedicao().name() : null;
        dto.bpm = h.getBpm();
        dto.temperatura = h.getTemperatura();
        // avaliacao registada no momento da medicao, com os limites em vigor
        // nessa altura; nao se recalcula aqui para nao duplicar a regra
        dto.avaliacao = h.getAvaliacao();
        dto.dataLeitura = h.getDataLeitura().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        return dto;
    }
}
