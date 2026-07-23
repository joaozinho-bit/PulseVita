package com.pulsevita.pulsevita.controller;

import com.pulsevita.pulsevita.model.PacienteDoenca;
import com.pulsevita.pulsevita.service.DoencaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/doencas")
@Tag(name = "Doenças", description = "Consulta das doenças associadas ao paciente autenticado")
public class DoencaController {

    private final DoencaService service;

    public DoencaController(DoencaService service) {
        this.service = service;
    }

    // doencas do paciente autenticado, ativas primeiro e depois por data de
    // diagnostico mais recente; devolve um DTO proprio, sem expor as entidades
    @Operation(summary = "Lista as doenças do paciente autenticado")
    @GetMapping("/minhas")
    public ResponseEntity<?> minhasDoencas(HttpSession session) {
        Long idPaciente = (Long) session.getAttribute("pacienteId");
        if (idPaciente == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<DoencaDTO> lista = service.listarDoencasDoPaciente(idPaciente).stream()
                .map(this::paraDTO)
                .sorted(Comparator
                        .comparingInt((DoencaDTO d) -> "ATIVA".equals(d.estado) ? 0 : 1)
                        .thenComparing(d -> d.dataDiagnostico == null ? "" : d.dataDiagnostico,
                                Comparator.reverseOrder()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(lista);
    }

    static class DoencaDTO {
        public Long id;
        public String nome;
        public Boolean cronica;
        public String observacoes;
        public String dataDiagnostico;
        public String dataFim;          // nulo quando a doenca esta ativa
        public String medico;
        public String estado;           // ATIVA ou TERMINADA
    }

    private DoencaDTO paraDTO(PacienteDoenca pd) {
        DoencaDTO dto = new DoencaDTO();
        dto.id = pd.getIdDoenca();
        if (pd.getDoenca() != null) {
            dto.nome = pd.getDoenca().getNome();
            dto.cronica = pd.getDoenca().getCronica();
            dto.observacoes = pd.getDoenca().getObservacoes();
        }
        dto.dataDiagnostico = pd.getDataDiagnostico() != null
                ? pd.getDataDiagnostico().format(DateTimeFormatter.ISO_DATE) : null;
        dto.dataFim = pd.getDataFim() != null
                ? pd.getDataFim().format(DateTimeFormatter.ISO_DATE) : null;
        dto.medico = pd.getMedico() != null ? pd.getMedico().getNome() : null;
        dto.estado = pd.getDataFim() == null ? "ATIVA" : "TERMINADA";
        return dto;
    }
}
