package com.pulsevita.pulsevita.service;

import com.pulsevita.pulsevita.model.PacienteDoenca;
import com.pulsevita.pulsevita.repository.PacienteDoencaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoencaService {

    private final PacienteDoencaRepository repository;

    public DoencaService(PacienteDoencaRepository repository) {
        this.repository = repository;
    }

    public List<PacienteDoenca> listarDoencasDoPaciente(Long idPaciente) {
        return repository.findByIdPaciente(idPaciente);
    }
}
