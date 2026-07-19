package com.pulsevita.pulsevita.service;

import com.pulsevita.pulsevita.model.Medico;
import org.springframework.stereotype.Service;

import java.util.Optional;

// Guarda em memoria o resultado da ultima leitura de cartao, a espera de ser
// consumido pela pagina de login do website atraves de polling
@Service
public class LoginCartaoService {

    public enum ResultadoLogin { SUCESSO, CARTAO_INVALIDO }

    public static class EventoLogin {
        private final ResultadoLogin resultado;
        private final Medico medico;

        private EventoLogin(ResultadoLogin resultado, Medico medico) {
            this.resultado = resultado;
            this.medico = medico;
        }

        public ResultadoLogin getResultado() {
            return resultado;
        }

        public Medico getMedico() {
            return medico;
        }
    }

    private static final long VALIDADE_MS = 30_000;

    private EventoLogin eventoPendente;
    private long timestampPendente;

    public synchronized void registarLoginPendente(Medico medico) {
        this.eventoPendente = new EventoLogin(ResultadoLogin.SUCESSO, medico);
        this.timestampPendente = System.currentTimeMillis();
    }

    public synchronized void registarCartaoInvalido() {
        this.eventoPendente = new EventoLogin(ResultadoLogin.CARTAO_INVALIDO, null);
        this.timestampPendente = System.currentTimeMillis();
    }

    // devolve o evento pendente se ainda for valido e consome-o (uso unico)
    public synchronized Optional<EventoLogin> consumirEvento() {
        if (eventoPendente == null) {
            return Optional.empty();
        }
        EventoLogin evento = eventoPendente;
        long idade = System.currentTimeMillis() - timestampPendente;
        eventoPendente = null;
        if (idade > VALIDADE_MS) {
            return Optional.empty();
        }
        return Optional.of(evento);
    }
}
