package com.pulsevita.pulsevita.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "utilizador", schema = "pulsevita") 

public class Utilizador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_completo") 
    private String nomeCompleto;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;
    

    @Column(name = "num_utente")
    private String numUtente;

    private String genero;
    private String username;
    private String email;
    private String telefone;
    private String password;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
    public String getNumUtente() {
        return numUtente;
    }
    public void setNumUtente(String numUtente) {
        this.numUtente = numUtente;
    }
}