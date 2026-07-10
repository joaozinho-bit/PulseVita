package com.pulsevita.pulsevita.service;

import com.pulsevita.pulsevita.model.Dispositivo;
import com.pulsevita.pulsevita.model.Paciente;
import com.pulsevita.pulsevita.repository.DispositivoRepository;
import com.pulsevita.pulsevita.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class DispositivoService {

    @Autowired
    private DispositivoRepository dispositivoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    public Paciente associar(Long idPaciente, String idDispositivo) {
        Paciente paciente = pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente não encontrado."));

        if (paciente.getDispositivo() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este paciente já tem um dispositivo associado.");
        }

        Dispositivo dispositivo = dispositivoRepository.findByIdDispositivo(idDispositivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dispositivo não encontrado."));

        if (pacienteRepository.existsByDispositivoId(dispositivo.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Dispositivo já associado a outra conta.");
        }

        paciente.setDispositivo(dispositivo);
        paciente.setDataAssociacao(LocalDateTime.now());

        return pacienteRepository.save(paciente);
    }

    public Paciente desassociar(Long idPaciente) {
        Paciente paciente = pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente não encontrado."));

        if (paciente.getDispositivo() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhum dispositivo associado a este paciente.");
        }

        paciente.setDispositivo(null);
        paciente.setDataAssociacao(null);

        return pacienteRepository.save(paciente);
    }

    public Paciente obter(Long idPaciente) {
        return pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente não encontrado."));
    }
}