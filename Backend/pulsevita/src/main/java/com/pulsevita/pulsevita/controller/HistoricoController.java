package com.pulsevita.pulsevita.controller;

import com.pulsevita.pulsevita.service.HistoricoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/historico")
@Tag(name = "Histórico de Medições", description = "Consulta do histórico de medições do paciente autenticado")
public class HistoricoController {

    @Autowired
    private HistoricoService service;

    // o paciente vem da sessao, como nos restantes endpoints da app,
    // para nenhum utilizador poder consultar o historico de outro
    @Operation(summary = "Lista o histórico de medições do paciente autenticado, com filtros opcionais de tipo, intervalo de datas e ordenação")
    @GetMapping
    public ResponseEntity<?> listar(
            HttpSession session,
            @Parameter(description = "Tipo de medição a devolver: TEMPERATURA, BPM ou AMBOS; omitido devolve todas")
            @RequestParam(required = false) String tipo,
            @Parameter(description = "Data inicial do intervalo (formato yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @Parameter(description = "Data final do intervalo (formato yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @Parameter(description = "Ordenação por data: recentes (defeito) ou antigas")
            @RequestParam(required = false) String ordem
    ) {
        Long idPaciente = (Long) session.getAttribute("pacienteId");
        if (idPaciente == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(service.listarHistorico(idPaciente, tipo, dataInicio, dataFim, ordem));
    }
}
