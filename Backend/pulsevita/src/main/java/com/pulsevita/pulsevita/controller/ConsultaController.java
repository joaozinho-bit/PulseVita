package com.pulsevita.pulsevita.controller;

import com.pulsevita.pulsevita.model.Consulta;
import com.pulsevita.pulsevita.service.ConsultaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/consultas")
public class ConsultaController {

    @Autowired
    private ConsultaService service;

    static class MarcarRequest {
        public Long idPaciente;
        public LocalDate data;
        public LocalTime hora;
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
}