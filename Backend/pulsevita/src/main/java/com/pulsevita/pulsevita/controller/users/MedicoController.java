package com.pulsevita.pulsevita.controller.users;

import com.pulsevita.pulsevita.model.Medico;
import com.pulsevita.pulsevita.service.LoginCartaoService;
import com.pulsevita.pulsevita.service.MedicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.util.Optional;


@RestController
@RequestMapping("/medicos")
public class MedicoController {

    @Autowired
    private MedicoService service;

    @Autowired
    private LoginCartaoService loginCartaoService;

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

    // Polling da pagina de login: 200 com o medico se um cartao valido foi lido,
    // 401 se o cartao lido nao corresponde a nenhum medico, 204 se nao ha novidades
    @GetMapping("/login-cartao/estado")
    public ResponseEntity<?> estadoLoginCartao(HttpSession session) {
        Optional<LoginCartaoService.EventoLogin> evento = loginCartaoService.consumirEvento();
        if (evento.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        if (evento.get().getResultado() == LoginCartaoService.ResultadoLogin.CARTAO_INVALIDO) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Cartão não reconhecido.");
        }
        Medico medico = evento.get().getMedico();
        session.setAttribute("medicoId", medico.getId());
        return ResponseEntity.ok(medico);
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