package com.pulsevita.pulsevita.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Limites personalizados definidos pelo medico para um paciente.
// Quando nao existe registo, o backend usa os valores padrao.
@Entity
@Table(name = "valores_referencia_paciente", schema = "pulsevita")
public class ValoresReferenciaPaciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_paciente")
    private Long idPaciente;

    @Column(name = "id_medico")
    private Long idMedico;

    @Column(name = "bpm_minimo")
    private Integer bpmMinimo;

    @Column(name = "bpm_maximo")
    private Integer bpmMaximo;

    @Column(name = "temperatura_maxima")
    private Double temperaturaMaxima;

    @Column(name = "data_definicao")
    private LocalDateTime dataDefinicao;

    public Long getId() {
        return id;
    }

    public Long getIdPaciente() {
        return idPaciente;
    }

    public Long getIdMedico() {
        return idMedico;
    }

    public Integer getBpmMinimo() {
        return bpmMinimo;
    }

    public Integer getBpmMaximo() {
        return bpmMaximo;
    }

    public Double getTemperaturaMaxima() {
        return temperaturaMaxima;
    }

    public LocalDateTime getDataDefinicao() {
        return dataDefinicao;
    }
}
