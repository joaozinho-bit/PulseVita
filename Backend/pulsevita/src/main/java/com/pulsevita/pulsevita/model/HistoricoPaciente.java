package com.pulsevita.pulsevita.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historico_paciente", schema = "pulsevita")
public class HistoricoPaciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_paciente")
    private Long idPaciente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_medicao")
    private TipoMedicao tipoMedicao;

    private Double temperatura;

    private Integer bpm;

    // avaliacao calculada no momento da medicao, com os limites em vigor nessa
    // altura; nao se recalcula ao consultar porque os limites podem mudar
    private String avaliacao;

    @Column(name = "data_leitura")
    private LocalDateTime dataLeitura;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(Long idPaciente) {
        this.idPaciente = idPaciente;
    }

    public TipoMedicao getTipoMedicao() {
        return tipoMedicao;
    }

    public void setTipoMedicao(TipoMedicao tipoMedicao) {
        this.tipoMedicao = tipoMedicao;
    }

    public Double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(Double temperatura) {
        this.temperatura = temperatura;
    }

    public Integer getBpm() {
        return bpm;
    }

    public void setBpm(Integer bpm) {
        this.bpm = bpm;
    }

    public String getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(String avaliacao) {
        this.avaliacao = avaliacao;
    }

    public LocalDateTime getDataLeitura() {
        return dataLeitura;
    }

    public void setDataLeitura(LocalDateTime dataLeitura) {
        this.dataLeitura = dataLeitura;
    }
}
