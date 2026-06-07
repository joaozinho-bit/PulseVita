package com.pulsevita.pulsevita.service;

import com.pulsevita.pulsevita.model.Utilizador;
import com.pulsevita.pulsevita.repository.UtilizadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UtilizadorService {

    @Autowired
    private UtilizadorRepository repository;
    // Método para fazer login
    public Utilizador fazerLoginComDados(String email, String senha) {
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
}

