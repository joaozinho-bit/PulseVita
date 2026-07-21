package com.pulsevita.pulsevita.controller;

import com.pulsevita.pulsevita.model.TipoMedicao;
import com.pulsevita.pulsevita.service.MedicaoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/medicoes")
public class MedicaoController {

    @Autowired
    private MedicaoService service;

    static class IniciarRequest {
        public String tipo;
    }

    @PostMapping("/iniciar")
    public ResponseEntity<?> iniciar(@RequestBody IniciarRequest body, HttpSession session) {
        Long idPaciente = (Long) session.getAttribute("pacienteId");
        if (idPaciente == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        TipoMedicao tipo;
        try {
            tipo = TipoMedicao.valueOf(body.tipo);
        } catch (IllegalArgumentException | NullPointerException e) {
            return ResponseEntity.badRequest().body("Tipo de medição inválido.");
        }

        return ResponseEntity.accepted().body(service.iniciar(idPaciente, tipo));
    }

    // polling da app: 200 com o estado atual, 204 quando nao ha medicao
    @GetMapping("/estado")
    public ResponseEntity<?> estado(HttpSession session) {
        Long idPaciente = (Long) session.getAttribute("pacienteId");
        if (idPaciente == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, Object> estado = service.consultarEstado(idPaciente);
        if (estado == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(estado);
    }

    @PostMapping("/cancelar")
    public ResponseEntity<?> cancelar(HttpSession session) {
        Long idPaciente = (Long) session.getAttribute("pacienteId");
        if (idPaciente == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        service.cancelar(idPaciente);
        return ResponseEntity.ok().build();
    }
}
