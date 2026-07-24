package com.pulsevita.pulsevita.controller;

import com.pulsevita.pulsevita.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Resumo do ecra inicial do paciente autenticado, num unico pedido.
@RestController
@RequestMapping("/dashboard")
@Tag(name = "Dashboard", description = "Resumo do ecrã inicial do paciente autenticado")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    // o paciente vem da sessao, como nos restantes endpoints da app
    @Operation(summary = "Devolve o resumo do dashboard: utilizador, próxima consulta e última medição")
    @GetMapping
    public ResponseEntity<?> resumo(HttpSession session) {
        Long idPaciente = (Long) session.getAttribute("pacienteId");
        if (idPaciente == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(service.montar(idPaciente));
    }
}
