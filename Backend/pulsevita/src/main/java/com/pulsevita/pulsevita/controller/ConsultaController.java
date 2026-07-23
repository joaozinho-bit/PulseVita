package com.pulsevita.pulsevita.controller;

import com.pulsevita.pulsevita.model.Consulta;
import com.pulsevita.pulsevita.service.ConsultaService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        Consulta consulta = service.marcarConsulta(dados.idPaciente, dados.data, dados.hora, dados.motivo);
        if (consulta != null) {
            return ResponseEntity.ok(consulta);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Paciente não encontrado.");
    }

    @GetMapping("/paciente/{id}")
    public ResponseEntity<?> listar(@PathVariable Long id) {
        return ResponseEntity.ok(service.listarConsultasConfirmadas(id));
    }

    // ecra de marcacoes do paciente autenticado: todas as consultas, num DTO
    // proprio que nao expoe as entidades paciente/medico completas
    @GetMapping("/minhas")
    public ResponseEntity<?> minhasConsultas(HttpSession session) {
        Long idPaciente = (Long) session.getAttribute("pacienteId");
        if (idPaciente == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<MinhaConsultaDTO> lista = service.listarConsultasDoPaciente(idPaciente)
                .stream().map(this::paraMinhaDTO).collect(Collectors.toList());
        return ResponseEntity.ok(lista);
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

    /*static class ConfirmarRequest {
        public Long idMedico;
    }*/

    @PostMapping("/{id}/confirmar")
    public ResponseEntity<?> confirmar(@PathVariable Long id, HttpSession session) {
        Long idMedico = (Long) session.getAttribute("medicoId");
        if (idMedico == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sessão de médico inválida.");
        }

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

        if (c.getPaciente() != null) {
            var p = c.getPaciente();
            var pac = new ConsultaMedicoDTO.PacienteDTO();
            pac.nome = p.getNomeCompleto();
            pac.genero = p.getGenero();
            pac.telefone = p.getTelefone();
            pac.email = p.getEmail();
            pac.idade = p.getDataNascimento() != null
                    ? Period.between(p.getDataNascimento(), LocalDate.now()).getYears()
                    : null;
            dto.paciente = pac;
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

    // ---- Vista do paciente ----

    static class MinhaConsultaDTO {
        public Long id;
        public String data;
        public String hora;
        public String estado;         // POR_CONFIRMAR, CONFIRMADA, CONCLUIDA ou CANCELADA
        public String motivo;
        public String medico;         // nulo enquanto a consulta nao for confirmada
        public String especialidade;  // nulo enquanto a consulta nao for confirmada
    }

    private MinhaConsultaDTO paraMinhaDTO(Consulta c) {
        MinhaConsultaDTO dto = new MinhaConsultaDTO();
        dto.id = c.getId();
        dto.data = c.getDataConsulta() != null ? c.getDataConsulta().format(DateTimeFormatter.ISO_DATE) : null;
        dto.hora = c.getHoraConsulta() != null ? c.getHoraConsulta().format(DateTimeFormatter.ofPattern("HH:mm")) : null;
        dto.estado = estadoEfetivo(c);
        dto.motivo = c.getMotivo();
        if (c.getMedico() != null) {
            dto.medico = c.getMedico().getNome();
            dto.especialidade = c.getMedico().getEspecializacao();
        }
        return dto;
    }

    // uma consulta confirmada cuja data e hora ja passaram e apresentada como concluida
    private String estadoEfetivo(Consulta c) {
        String estado = c.getEstado();
        if ("CONFIRMADA".equals(estado) && c.getDataConsulta() != null) {
            LocalDateTime quando = c.getHoraConsulta() != null
                    ? c.getDataConsulta().atTime(c.getHoraConsulta())
                    : c.getDataConsulta().atStartOfDay();
            if (quando.isBefore(LocalDateTime.now())) {
                return "CONCLUIDA";
            }
        }
        return estado != null ? estado : "POR_CONFIRMAR";
    }
}