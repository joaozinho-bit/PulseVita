package com.pulsevita.pulsevita.model;

import java.io.Serializable;
import java.util.Objects;

// chave composta da tabela de associacao paciente_doenca
public class PacienteDoencaId implements Serializable {

    private Long idPaciente;
    private Long idDoenca;

    public PacienteDoencaId() {
    }

    public PacienteDoencaId(Long idPaciente, Long idDoenca) {
        this.idPaciente = idPaciente;
        this.idDoenca = idDoenca;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PacienteDoencaId)) return false;
        PacienteDoencaId that = (PacienteDoencaId) o;
        return Objects.equals(idPaciente, that.idPaciente)
                && Objects.equals(idDoenca, that.idDoenca);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPaciente, idDoenca);
    }
}
