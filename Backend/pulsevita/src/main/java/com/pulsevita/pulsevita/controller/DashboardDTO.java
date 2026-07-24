package com.pulsevita.pulsevita.controller;

// Resumo do dashboard do paciente, agregado num so pedido para o ecra inicial
// nao precisar de varios fetches consecutivos (utilizador + proxima consulta +
// ultima medicao). A logica vive no DashboardService, que reutiliza os servicos
// existentes; aqui e apenas o formato devolvido ao frontend.
public class DashboardDTO {

    public Utilizador utilizador;
    public ProximaConsulta proximaConsulta;   // nulo quando nao ha consulta futura
    public Medicao ultimaMedicao;             // nula quando o paciente ainda nao mediu
    public boolean medicaoDeHoje;             // true se a ultima medicao e de hoje

    // so o essencial para o cabecalho do dashboard; email/telefone ficam nas Definicoes
    public static class Utilizador {
        public String nome;
        public String nPaciente;
        public String fotoPerfil;
    }

    public static class ProximaConsulta {
        public String data;          // yyyy-MM-dd
        public String hora;          // HH:mm
        public String medico;        // preenchido apenas apos confirmacao
        public String especialidade;
        public String estado;
    }

    public static class Medicao {
        public String data;          // yyyy-MM-dd
        public String hora;          // HH:mm
        public Double temperatura;   // nula se a medicao nao inclui temperatura
        public Integer bpm;          // nulo se a medicao nao inclui BPM
        public String avaliacao;     // NORMAL / FEBRE / BPM_ALTO / BPM_BAIXO (ou combinacoes)
        public String tipoMedicao;   // TEMPERATURA / BPM / AMBOS
    }
}
