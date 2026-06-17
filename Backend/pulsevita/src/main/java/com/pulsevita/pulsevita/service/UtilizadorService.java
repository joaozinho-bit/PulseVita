package com.pulsevita.pulsevita.service;
import java.util.Optional;
import com.pulsevita.pulsevita.model.Utilizador;
import com.pulsevita.pulsevita.repository.UtilizadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UtilizadorService {

    @Autowired
    private UtilizadorRepository repository;
    // Método para fazer login
    public Utilizador fazerLogin(String email, String senha) {
        Utilizador utilizador = repository.findByEmail(email);
        if (utilizador == null) return null;
        if (utilizador.getPassword().equals(senha)) return utilizador;
        return null;
    }

    public boolean registarUtilizador(String nomeCompleto, String email, String senha) {
    Utilizador existente = repository.findByEmail(email);
    if (existente != null) {
        return false;
    }
    Utilizador novo = new Utilizador();
    novo.setNomeCompleto(nomeCompleto);
    novo.setEmail(email);
    novo.setPassword(senha);
    repository.save(novo);
    return true;

    
    }

    public Optional<Utilizador> getUtilizador(Long id) {
    return repository.findById(id);
}

public Utilizador atualizarUtilizador(Long id, String nome, String telefone, String email, String n_utente) {
    return repository.findById(id).map(u -> {
        if (nome != null) u.setNomeCompleto(nome);
        if (telefone != null) u.setTelefone(telefone);
        if (email != null) u.setEmail(email);
        if (n_utente != null) u.setN_utente(n_utente);
        return repository.save(u);
    }).orElse(null);
}
}

