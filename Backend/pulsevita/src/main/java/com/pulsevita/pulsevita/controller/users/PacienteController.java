package com.pulsevita.pulsevita.controller.users;

import com.pulsevita.pulsevita.model.Paciente;
import com.pulsevita.pulsevita.service.PacienteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class PacienteController {

    @Autowired
    private PacienteService service;

    static class LoginRequest {
        public String email;
        public String senha;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginData, HttpSession session) {
        Paciente paciente = service.fazerLogin(loginData.email, loginData.senha);

        if (paciente != null) {
            session.setAttribute("pacienteId", paciente.getId());
            return ResponseEntity.ok(paciente);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou senha incorretos.");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getPacienteAtual(HttpSession session) {
        Long id = (Long) session.getAttribute("pacienteId");
        if (id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return service.getPaciente(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().build();
    }

    static class RegisterRequest {
    public String nomeCompleto;
    public String email;
    public String senha;
    public String genero;
    public LocalDate dataNasc;
}

@PostMapping("/register")
public ResponseEntity<String> register(@RequestBody RegisterRequest dados) {
    boolean sucesso = service.registarPaciente(
        dados.nomeCompleto, dados.email, dados.senha, dados.genero, dados.dataNasc
    );
    if (sucesso) {
        return ResponseEntity.ok("Conta criada");
    } else {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Email já existe.");
    }
}

    @GetMapping("/{id}")
    public ResponseEntity<?> getPaciente(@PathVariable Long id) {
        return service.getPaciente(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    static class UpdateRequest {
        public String nomeCompleto;
        public String telefone;
        public String email;
        public String n_paciente;
        public String genero;
        public LocalDate dataNasc;
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePaciente(@PathVariable Long id, @RequestBody UpdateRequest dados) {
        Paciente atualizado = service.atualizarPaciente(
            id, dados.nomeCompleto, dados.telefone, dados.email,
            dados.n_paciente, dados.genero, dados.dataNasc
        );
        if (atualizado != null) {
            return ResponseEntity.ok(atualizado);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/foto")
public ResponseEntity<?> uploadFoto(@PathVariable Long id, @RequestParam("foto") MultipartFile file) {
    try {
        String nomeFicheiro = service.guardarFoto(id, file);
        if (nomeFicheiro == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("foto_perfil", nomeFicheiro));
    } catch (IOException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao guardar a imagem.");
    }
}
}