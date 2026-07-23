package com.pulsevita.pulsevita.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "paciente_doenca", schema = "pulsevita")
@IdClass(PacienteDoencaId.class)
public class PacienteDoenca {

    @Id
    @Column(name = "id_paciente")
    private Long idPaciente;

    @Id
    @Column(name = "id_doenca")
    private Long idDoenca;

    @Column(name = "data_diagnostico")
    private LocalDate dataDiagnostico;

    // nulo enquanto a doenca esta ativa; preenchido quando termina
    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(name = "id_medico")
    private Long idMedico;

    // relacoes so de leitura sobre as mesmas colunas, para obter o nome da
    // doenca e do medico sem gerir estas associacoes ao gravar
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_doenca", insertable = false, updatable = false)
    private Doenca doenca;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_medico", insertable = false, updatable = false)
    private Medico medico;

    public Long getIdPaciente() {
        return idPaciente;
    }

    public Long getIdDoenca() {
        return idDoenca;
    }

    public LocalDate getDataDiagnostico() {
        return dataDiagnostico;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public Doenca getDoenca() {
        return doenca;
    }

    public Medico getMedico() {
        return medico;
    }
}
