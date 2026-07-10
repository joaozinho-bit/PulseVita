package com.pulsevita.pulsevita.controller;

import com.pulsevita.pulsevita.model.Consulta;
import com.pulsevita.pulsevita.service.ConsultaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/consultas")
public class ConsultaController {

    @Autowired
    private ConsultaService service;

    static class MarcarRequest {
        public Long idPaciente;
        public LocalDate data;
        public LocalTime hora;
        public String motivo;
    }

    @PostMapping
    public ResponseEntity<?> marcar(@RequestBody MarcarRequest dados) {
        Consulta consulta = service.marcarConsulta(dados.idPaciente, dados.data, dados.hora);
        if (consulta != null) {
            return ResponseEntity.ok(consulta);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Paciente não encontrado.");
    }

    @GetMapping("/paciente/{id}")
    public ResponseEntity<?> listar(@PathVariable Long id) {
        return ResponseEntity.ok(service.listarConsultasConfirmadas(id));
    }

    @GetMapping("/paciente/{id}/proxima")
    public ResponseEntity<?> proxima(@PathVariable Long id) {
        return service.proximaConsulta(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    // ---- Calendário do médico ----

    @GetMapping("/medico/mes")
    public ResponseEntity<?> listarPorMes(@RequestParam int ano, @RequestParam int mes) {
        List<ConsultaMedicoDTO> lista = service.listarConsultasDoMes(ano, mes)
                .stream().map(this::paraDTO).collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    static class ConfirmarRequest {
        public Long idMedico;
    }

    @PostMapping("/{id}/confirmar")
    public ResponseEntity<?> confirmar(@PathVariable Long id, @RequestBody(required = false) ConfirmarRequest body) {
        Long idMedico = body != null ? body.idMedico : null;
        Consulta consulta = service.confirmarConsulta(id, idMedico);
        if (consulta == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(paraDTO(consulta));
    }

    @PostMapping("/{id}/recusar")
    public ResponseEntity<?> recusar(@PathVariable Long id) {
        Consulta consulta = service.recusarConsulta(id);
        if (consulta == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(paraDTO(consulta));
    }

    // ---- DTO ----

    static class ConsultaMedicoDTO {
        public Long id;
        public String data;
        public String hora;
        public String estado;
        public String motivo;
        public PacienteDTO paciente;

        static class PacienteDTO {
            public String nome;
            public Integer idade;
            public String genero;
            public String telefone;
            public String email;
        }
    }

    private ConsultaMedicoDTO paraDTO(Consulta c) {
        ConsultaMedicoDTO dto = new ConsultaMedicoDTO();
        dto.id = c.getId();
        dto.data = c.getDataConsulta() != null ? c.getDataConsulta().format(DateTimeFormatter.ISO_DATE) : null;
        dto.hora = c.getHoraConsulta() != null ? c.getHoraConsulta().format(DateTimeFormatter.ofPattern("HH:mm")) : null;
        dto.estado = mapearEstado(c.getEstado());
        dto.motivo = c.getMotivo();

        if (c.getUtilizador() != null) {
            var u = c.getUtilizador();
            var p = new ConsultaMedicoDTO.PacienteDTO();
            p.nome = u.getNomeCompleto();
            p.genero = u.getGenero();
            p.telefone = u.getTelefone();
            p.email = u.getEmail();
            p.idade = u.getDataNascimento() != null
                    ? Period.between(u.getDataNascimento(), LocalDate.now()).getYears()
                    : null;
            dto.paciente = p;
        }
        return dto;
    }

    private String mapearEstado(String estadoBd) {
        if (estadoBd == null) return "pendente";
        return switch (estadoBd) {
            case "CONFIRMADA", "CONCLUIDA" -> "aprovado";
            case "CANCELADA" -> "recusado";
            default -> "pendente"; // POR_CONFIRMAR
        };
    }
}