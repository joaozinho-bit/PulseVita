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
@CrossOrigin(origins = "*")
public class ConsultaController {

    @Autowired
    private ConsultaService service;

    static class MarcarRequest {
        public Long idUtilizador;
        public LocalDate data;
        public LocalTime hora;
    }

    @PostMapping
    public ResponseEntity<?> marcar(@RequestBody MarcarRequest dados) {
        Consulta consulta = service.marcarConsulta(dados.idUtilizador, dados.data, dados.hora);
        if (consulta != null) {
            return ResponseEntity.ok(consulta);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilizador não encontrado.");
    }

    @GetMapping("/utilizador/{id}")
    public ResponseEntity<?> listar(@PathVariable Long id) {
        return ResponseEntity.ok(service.listarConsultasConfirmadas(id));
    }

    @GetMapping("/utilizador/{id}/proxima")
    public ResponseEntity<?> proxima(@PathVariable Long id) {
        return service.proximaConsulta(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}