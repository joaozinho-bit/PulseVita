package com.pulsevita.pulsevita.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "paciente", schema = "pulsevita") 

public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_completo") 
    private String nomeCompleto;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;
    
    @Column(name = "n_paciente")
    private String n_paciente;

    @Column(name = "genero")
    private String genero;

    @Column(name = "email")
    private String email;
    @Column(name = "telefone")  
    private String telefone;
    
    @Column(name = "password")
    private String password;

    @ManyToOne
    @JoinColumn(name = "id_dispositivo")
    private Dispositivo dispositivo;

    @Column(name = "data_associacao")
    private LocalDateTime dataAssociacao;

    // Gets e Sets
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public String getTelefone() {
        return telefone;
    }
    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
    public String getN_paciente() {
        return n_paciente;
    }
    public void setN_paciente(String n_paciente) {
        this.n_paciente = n_paciente;
    }

    // ***** DISPOSITIVO *****

    public Dispositivo getDispositivo() { 
        return dispositivo; 
    }
    public void setDispositivo(Dispositivo dispositivo) {
        this.dispositivo = dispositivo; 
    }

    public LocalDateTime getDataAssociacao() {
        return dataAssociacao; 
    
    }
    
    public void setDataAssociacao(LocalDateTime dataAssociacao) {
        this.dataAssociacao = dataAssociacao; 
    }
}