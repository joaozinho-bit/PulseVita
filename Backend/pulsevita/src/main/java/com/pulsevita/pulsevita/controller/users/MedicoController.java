package com.pulsevita.pulsevita.controller.users;

import com.pulsevita.pulsevita.model.Medico;
import com.pulsevita.pulsevita.service.MedicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;


@RestController
@RequestMapping("/medicos")
public class MedicoController {

    @Autowired
    private MedicoService service;

    static class LoginRequest {
        public String numero_medico;
        public String password;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginData, HttpSession session) {
        Medico medico = service.fazerLogin(loginData.numero_medico, loginData.password);

        if (medico != null) {
            session.setAttribute("medicoId", medico.getId());
            return ResponseEntity.ok(medico);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Número de médico ou senha incorretos.");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMedico(HttpSession session) {
        Long id = (Long) session.getAttribute("medicoId");
        if (id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return service.getMedico(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}