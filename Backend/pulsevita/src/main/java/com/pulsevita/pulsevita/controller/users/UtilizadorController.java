
package com.pulsevita.pulsevita.controller.users;

import com.pulsevita.pulsevita.model.Utilizador;
import com.pulsevita.pulsevita.service.UtilizadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UtilizadorController {

    @Autowired
    private UtilizadorService service;

    static class LoginRequest {
        public String email;
        public String senha;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginData) {
        Utilizador utilizador = service.fazerLogin(loginData.email, loginData.senha);

        if (utilizador != null) {
            return ResponseEntity.ok(utilizador);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou senha incorretos.");
        }
}

    static class RegisterRequest {
    public String nomeCompleto;
    public String email;
    public String senha;
}

@PostMapping("/register")
public ResponseEntity<String> register(@RequestBody RegisterRequest dados) {
    boolean sucesso = service.registarUtilizador(dados.nomeCompleto, dados.email, dados.senha);
    if (sucesso) {
        return ResponseEntity.ok("Conta criada");
    } else {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Email já existe.");
    }
}

@GetMapping("/{id}")
public ResponseEntity<?> getUtilizador(@PathVariable Long id) {
    return service.getUtilizador(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
}

static class UpdateRequest {
    public String nomeCompleto;
    public String telefone;
    public String email;
    public String numUtente;
}

@PutMapping("/{id}")
public ResponseEntity<?> updateUtilizador(@PathVariable Long id, @RequestBody UpdateRequest dados) {
    Utilizador atualizado = service.atualizarUtilizador(id, dados.nomeCompleto, dados.telefone, dados.email, dados.numUtente);
    if (atualizado != null) {
        return ResponseEntity.ok(atualizado);
    }
    return ResponseEntity.notFound().build();
}



}