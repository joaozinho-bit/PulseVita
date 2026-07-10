package com.pulsevita.pulsevita.controller;

import com.pulsevita.pulsevita.model.Paciente;
import com.pulsevita.pulsevita.service.DispositivoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class DispositivoController {

    @Autowired
    private DispositivoService dispositivoService;

    @GetMapping("/{idPaciente}/dispositivo")
    public ResponseEntity<?> obter(@PathVariable Long idPaciente) {
        Paciente paciente = dispositivoService.obter(idPaciente);

        if (paciente.getDispositivo() == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(Map.of(
                "idDispositivo", paciente.getDispositivo().getIdDispositivo(),
                "dataAssociacao", paciente.getDataAssociacao().toString()
        ));
    }

    @PostMapping("/{idPaciente}/dispositivo")
    public ResponseEntity<?> associar(
            @PathVariable Long idPaciente,
            @RequestBody Map<String, String> body) {

        String idDispositivo = body.get("idDispositivo");
        if (idDispositivo == null || idDispositivo.isBlank()) {
            return ResponseEntity.badRequest().body("O campo idDispositivo é obrigatório.");
        }

        Paciente paciente = dispositivoService.associar(idPaciente, idDispositivo);

        return ResponseEntity.ok(Map.of(
                "idDispositivo", paciente.getDispositivo().getIdDispositivo(),
                "dataAssociacao", paciente.getDataAssociacao().toString()
        ));
    }

    @DeleteMapping("/{idPaciente}/dispositivo")
    public ResponseEntity<?> desassociar(@PathVariable Long idPaciente) {
        dispositivoService.desassociar(idPaciente);
        return ResponseEntity.noContent().build();
    }
}