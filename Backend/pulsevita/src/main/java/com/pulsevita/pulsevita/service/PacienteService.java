package com.pulsevita.pulsevita.service;
import java.util.Optional;
import com.pulsevita.pulsevita.model.Paciente;
import com.pulsevita.pulsevita.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PacienteService {

    @Autowired
    private PacienteRepository repository;
    // Método para fazer login
    public Paciente fazerLogin(String email, String senha) {
        Paciente paciente = repository.findByEmail(email);
        if (paciente == null) return null;
        if (paciente.getPassword().equals(senha)) return paciente;
        return null;
    }

    public boolean registarPaciente(String nomeCompleto, String email, String senha) {
    Paciente existente = repository.findByEmail(email);
    if (existente != null) {
        return false;
    }
    Paciente novo = new Paciente();
    novo.setNomeCompleto(nomeCompleto);
    novo.setEmail(email);
    novo.setPassword(senha);
    repository.save(novo);
    return true;

    
    }

    public Optional<Paciente> getPaciente(Long id) {
    return repository.findById(id);
}

public Paciente atualizarPaciente(Long id, String nome, String telefone, String email, String n_utente) {
    return repository.findById(id).map(u -> {
        if (nome != null) u.setNomeCompleto(nome);
        if (telefone != null) u.setTelefone(telefone);
        if (email != null) u.setEmail(email);
        if (n_utente != null) u.setN_utente(n_utente);
        return repository.save(u);
    }).orElse(null);
}
}

