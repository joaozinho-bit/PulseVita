package com.pulsevita.pulsevita.service;

import java.time.LocalDate;
import java.util.Optional;
import com.pulsevita.pulsevita.model.Paciente;
import com.pulsevita.pulsevita.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
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
        novo.setN_paciente(gerarNumeroPaciente());
        repository.save(novo);
        return true;
    }

   private String gerarNumeroPaciente() {
    String numero;
    do {
        numero = String.valueOf((long) (100000000 + Math.random() * 900000000)); // 9 dígitos
    } while (repository.findByNumeroPaciente(numero) != null);
    return numero;
}

    public Optional<Paciente> getPaciente(Long id) {
        return repository.findById(id);
    }

    public Paciente atualizarPaciente(Long id, String nome, String telefone, String email,
                                   String n_paciente, String genero, LocalDate dataNascimento) {
    return repository.findById(id).map(u -> {
        if (nome != null) u.setNomeCompleto(nome);
        if (telefone != null) u.setTelefone(telefone);
        if (email != null) u.setEmail(email);
        if (n_paciente != null) u.setN_paciente(n_paciente);
        if (genero != null) u.setGenero(genero);
        if (dataNascimento != null) u.setDataNascimento(dataNascimento);
        return repository.save(u);
    }).orElse(null);
    }

    public String guardarFoto(Long id, MultipartFile file) throws IOException {
    Paciente paciente = repository.findById(id).orElse(null);
    if (paciente == null) return null;

    // Apaga a foto anterior, se existir
    String fotoAntiga = paciente.getFotoPerfil();
    if (fotoAntiga != null && !fotoAntiga.isBlank()) {
        Path caminhoAntigo = Paths.get("uploads").resolve(fotoAntiga);
        Files.deleteIfExists(caminhoAntigo);
    }

    String extensao = "";
    String nomeOriginal = file.getOriginalFilename();
    if (nomeOriginal != null && nomeOriginal.contains(".")) {
        extensao = nomeOriginal.substring(nomeOriginal.lastIndexOf("."));
    }

    String nomeFicheiro = "paciente_" + id + "_" + UUID.randomUUID() + extensao;

    Path pastaUploads = Paths.get("uploads");
    if (!Files.exists(pastaUploads)) {
        Files.createDirectories(pastaUploads);
    }

    Path destino = pastaUploads.resolve(nomeFicheiro);
    Files.copy(file.getInputStream(), destino);

    paciente.setFotoPerfil(nomeFicheiro);
    repository.save(paciente);

    return nomeFicheiro;
    }
}