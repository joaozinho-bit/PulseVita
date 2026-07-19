package com.pulsevita.pulsevita.service;

import java.util.Optional;

import com.pulsevita.pulsevita.model.Medico;
import com.pulsevita.pulsevita.repository.MedicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class MedicoService {

    @Autowired
    private MedicoRepository repository;

    // Login sem cartão (número de médico + password)
    public Medico fazerLogin(String numeroMedico, String password) {
        Medico medico = repository.findByNumeroMedico(numeroMedico);
        if (medico == null) return null;
        if (medico.getPassword().equals(password)) return medico;
        return null;
    }

    // Login com cartão RFID lido pelo dispositivo
    public Medico loginPorCartao(String idCartao) {
        return repository.findByIdCartao(idCartao);
    }


    public Optional<Medico> getMedico(Long id) {
        return repository.findById(id);
    }
}