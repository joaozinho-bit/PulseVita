package com.pulsevita.pulsevita.model;

import jakarta.persistence.*;

@Entity
@Table(name = "doenca", schema = "pulsevita")
public class Doenca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private Boolean cronica;

    private String observacoes;

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Boolean getCronica() {
        return cronica;
    }

    public String getObservacoes() {
        return observacoes;
    }
}
