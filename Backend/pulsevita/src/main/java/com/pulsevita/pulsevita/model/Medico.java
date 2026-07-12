package com.pulsevita.pulsevita.model;

import jakarta.persistence.*;

@Entity
@Table(name = "medico", schema = "pulsevita")

public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String especializacao;

    @Column(name = "id_cartao")
    private Long idCartao;

    @Column(name = "n_medico")
    private String numeroMedico;

    @Column(name = "password")
    private String password;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEspecializacao() {
        return especializacao;
    }

    public void setEspecializacao(String especializacao) {
        this.especializacao = especializacao;
    }

    public Long getIdCartao() {
        return idCartao;
    }

    public void setIdCartao(Long idCartao) {
        this.idCartao = idCartao;
    }

    public String getNumeroMedico() {
        return numeroMedico;
    }

    public void setNumeroMedico(String numeroMedico) {
        this.numeroMedico = numeroMedico;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}