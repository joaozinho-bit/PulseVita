package com.pulsevita.pulsevita.controller;

import com.pulsevita.pulsevita.service.HistoricoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/historico")
@Tag(name = "Histórico de Medições", description = "Consulta do histórico de BPM e temperatura de um paciente")
public class HistoricoController {

    @Autowired
    private HistoricoService service;

    @Operation(summary = "Lista o histórico de medições de um paciente, com filtros opcionais de tipo e intervalo de datas")
    @GetMapping("/paciente/{id}")
    public ResponseEntity<List<HistoricoDTO>> listar(
            @PathVariable Long id,
            @Parameter(description = "Tipo de dado a devolver: BPM, TEMPERATURA ou AMBOS (defeito)")
            @RequestParam(required = false) String tipo,
            @Parameter(description = "Data inicial do intervalo (formato yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @Parameter(description = "Data final do intervalo (formato yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        return ResponseEntity.ok(service.listarHistorico(id, tipo, dataInicio, dataFim));
    }
}