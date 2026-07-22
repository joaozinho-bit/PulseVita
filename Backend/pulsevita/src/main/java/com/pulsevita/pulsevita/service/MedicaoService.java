package com.pulsevita.pulsevita.service;

import com.google.gson.JsonObject;
import com.pulsevita.pulsevita.model.HistoricoPaciente;
import com.pulsevita.pulsevita.model.Paciente;
import com.pulsevita.pulsevita.model.TipoMedicao;
import com.pulsevita.pulsevita.model.ValoresReferenciaPaciente;
import com.pulsevita.pulsevita.mqtt.MqttConfig;
import com.pulsevita.pulsevita.mqtt.MqttEnvelopeUtil;
import com.pulsevita.pulsevita.repository.HistoricoPacienteRepository;
import com.pulsevita.pulsevita.repository.PacienteRepository;
import com.pulsevita.pulsevita.repository.ValoresReferenciaPacienteRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Orquestra o ciclo de vida de uma medicao: pedido da app, comando ao
// dispositivo, confirmacao, resultado, avaliacao, persistencia e feedback.
// O estado vive em memoria (uma medicao de cada vez, prototipo com um
// dispositivo) e os timeouts sao avaliados de forma preguicosa em cada
// consulta de estado, dispensando schedulers.
@Service
public class MedicaoService {

    public enum Estado { AGUARDA_DISPOSITIVO, EM_CURSO, CONCLUIDA, ERRO }

    // limites padrao quando o medico nao definiu valores personalizados
    private static final double TEMPERATURA_MAXIMA_PADRAO = 37.5;
    private static final int BPM_MINIMO_PADRAO = 60;
    private static final int BPM_MAXIMO_PADRAO = 100;

    // janela de plausibilidade da temperatura compensada: fora disto o sensor
    // nao estava apontado a uma pessoa e a medicao e invalida
    private static final double TEMPERATURA_PLAUSIVEL_MINIMA = 33.0;
    private static final double TEMPERATURA_PLAUSIVEL_MAXIMA = 42.5;

    // plausibilidade dos BPM: valida apenas se a leitura e fisicamente possivel,
    // nunca entra na avaliacao clinica
    private static final int BPM_PLAUSIVEL_MINIMO = 30;
    private static final int BPM_PLAUSIVEL_MAXIMO = 220;

    private static final long TIMEOUT_DISPOSITIVO_MS = 6_000;
    private static final long TIMEOUT_RESULTADO_MS = 15_000;
    private static final long VALIDADE_RESULTADO_MS = 60_000;

    static class MedicaoEmCurso {
        long idMedicao;
        Long idPaciente;
        String deviceId;
        TipoMedicao tipo;
        Estado estado;
        String motivoErro;
        Double temperatura;
        Integer bpm;
        String avaliacao;
        Double temperaturaMaxima; // limites usados na avaliacao, mostrados ao utilizador
        Integer bpmMinimo;
        Integer bpmMaximo;
        String fase;              // fase reportada pelo dispositivo (TEMPERATURA/BPM)
        Integer valorParcial;     // BPM ao vivo durante a medicao
        Boolean dedo;
        Integer progresso;
        Integer validos;
        Integer alvo;
        long inicio;
        long ultimaAtualizacao;
    }

    private MedicaoEmCurso atual;

    // ids pequenos e crescentes: o ESP32 devolve o id num long de 32 bits,
    // por isso nao se usa System.currentTimeMillis() diretamente como id
    private final java.util.concurrent.atomic.AtomicLong geradorIdMedicao =
            new java.util.concurrent.atomic.AtomicLong(System.currentTimeMillis() % 1_000_000_000L);

    private final PacienteRepository pacienteRepository;
    private final HistoricoPacienteRepository historicoRepository;
    private final ValoresReferenciaPacienteRepository valoresReferenciaRepository;
    private final MqttConfig mqttConfig;

    // @Lazy quebra o ciclo: MqttConfig -> LeituraMqttListener -> MedicaoService -> MqttConfig
    public MedicaoService(PacienteRepository pacienteRepository,
                          HistoricoPacienteRepository historicoRepository,
                          ValoresReferenciaPacienteRepository valoresReferenciaRepository,
                          @Lazy MqttConfig mqttConfig) {
        this.pacienteRepository = pacienteRepository;
        this.historicoRepository = historicoRepository;
        this.valoresReferenciaRepository = valoresReferenciaRepository;
        this.mqttConfig = mqttConfig;
    }

    public synchronized Map<String, Object> iniciar(Long idPaciente, TipoMedicao tipo) {
        atualizarTimeouts();
        if (atual != null && (atual.estado == Estado.AGUARDA_DISPOSITIVO || atual.estado == Estado.EM_CURSO)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma medição em curso.");
        }

        Paciente paciente = pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Paciente não encontrado."));
        if (paciente.getDispositivo() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O paciente não tem um dispositivo associado.");
        }

        MedicaoEmCurso medicao = new MedicaoEmCurso();
        medicao.idMedicao = geradorIdMedicao.incrementAndGet();
        medicao.idPaciente = idPaciente;
        medicao.deviceId = paciente.getDispositivo().getIdDispositivo();
        medicao.tipo = tipo;
        medicao.estado = Estado.AGUARDA_DISPOSITIVO;
        medicao.inicio = System.currentTimeMillis();
        medicao.ultimaAtualizacao = medicao.inicio;

        JsonObject comando = new JsonObject();
        comando.addProperty("tipo", "iniciarMedicao");
        comando.addProperty("medicao", tipo.name());
        comando.addProperty("idMedicao", medicao.idMedicao);
        publicarComando(medicao.deviceId, comando);

        atual = medicao;
        return estadoComoMapa(medicao);
    }

    // chamado pelo listener MQTT quando o dispositivo confirma ou recusa o comando
    public synchronized void registarEstadoDispositivo(long idMedicao, String estado) {
        if (atual == null || atual.idMedicao != idMedicao) {
            return; // mensagem de uma medicao antiga, ignora
        }
        atual.ultimaAtualizacao = System.currentTimeMillis();

        if ("iniciada".equals(estado) && atual.estado == Estado.AGUARDA_DISPOSITIVO) {
            atual.estado = Estado.EM_CURSO;
        } else if ("erro".equals(estado)) {
            atual.estado = Estado.ERRO;
            atual.motivoErro = "ERRO_DISPOSITIVO";
        }
    }

    // chamado pelo listener MQTT quando chega o valor final medido
    public synchronized void registarResultado(long idMedicao, Double temperatura, Integer bpm) {
        if (atual == null || atual.idMedicao != idMedicao) {
            return;
        }
        atual.ultimaAtualizacao = System.currentTimeMillis();

        boolean temperaturaInvalida = temperatura != null
                && (temperatura < TEMPERATURA_PLAUSIVEL_MINIMA || temperatura > TEMPERATURA_PLAUSIVEL_MAXIMA);
        boolean bpmInvalido = bpm != null
                && (bpm < BPM_PLAUSIVEL_MINIMO || bpm > BPM_PLAUSIVEL_MAXIMO);

        if (temperaturaInvalida || bpmInvalido) {
            atual.estado = Estado.ERRO;
            atual.motivoErro = "LEITURA_INVALIDA";
            enviarRespostaDispositivo("INVALIDA");
            System.out.println("Medicao rejeitada por implausibilidade: temp=" + temperatura + " bpm=" + bpm);
            return;
        }

        String avaliacao = avaliar(atual.idPaciente, temperatura, bpm);

        HistoricoPaciente registo = new HistoricoPaciente();
        registo.setIdPaciente(atual.idPaciente);
        registo.setTipoMedicao(atual.tipo);
        registo.setTemperatura(temperatura);
        registo.setBpm(bpm);
        registo.setAvaliacao(avaliacao);
        registo.setDataLeitura(LocalDateTime.now());
        historicoRepository.save(registo);

        atual.temperatura = temperatura;
        atual.bpm = bpm;
        atual.avaliacao = avaliacao;
        atual.estado = Estado.CONCLUIDA;

        enviarRespostaDispositivo(avaliacao);
        System.out.println("Medicao concluida: " + temperatura + " C, avaliacao " + avaliacao);
    }

    // chamado pelo listener MQTT a cada mensagem de progresso do dispositivo;
    // alem de alimentar a app, refresca ultimaAtualizacao e funciona como
    // keep-alive: uma medicao longa de BPM nunca cai no timeout de resultado
    public synchronized void registarProgresso(long idMedicao, String fase, Integer bpm,
                                               Boolean dedo, Integer progresso,
                                               Integer validos, Integer alvo) {
        if (atual == null || atual.idMedicao != idMedicao) {
            return;
        }
        atual.ultimaAtualizacao = System.currentTimeMillis();
        atual.fase = fase;
        atual.valorParcial = bpm;
        atual.dedo = dedo;
        atual.progresso = progresso;
        atual.validos = validos;
        atual.alvo = alvo;
    }

    public synchronized Map<String, Object> consultarEstado(Long idPaciente) {
        atualizarTimeouts();
        if (atual == null || !atual.idPaciente.equals(idPaciente)) {
            return null;
        }
        return estadoComoMapa(atual);
    }

    public synchronized void cancelar(Long idPaciente) {
        if (atual == null || !atual.idPaciente.equals(idPaciente)) {
            return;
        }
        JsonObject comando = new JsonObject();
        comando.addProperty("tipo", "pararMedicao");
        try {
            publicarComando(atual.deviceId, comando);
        } catch (ResponseStatusException e) {
            // dispositivo inacessivel: o timeout do firmware limpa do lado dele
        }
        atual = null;
    }

    private void atualizarTimeouts() {
        if (atual == null) return;
        long agora = System.currentTimeMillis();

        if (atual.estado == Estado.AGUARDA_DISPOSITIVO && agora - atual.inicio > TIMEOUT_DISPOSITIVO_MS) {
            atual.estado = Estado.ERRO;
            atual.motivoErro = "DISPOSITIVO_NAO_RESPONDE";
            atual.ultimaAtualizacao = agora;
        } else if (atual.estado == Estado.EM_CURSO && agora - atual.ultimaAtualizacao > TIMEOUT_RESULTADO_MS) {
            atual.estado = Estado.ERRO;
            atual.motivoErro = "TIMEOUT";
            atual.ultimaAtualizacao = agora;
        } else if ((atual.estado == Estado.CONCLUIDA || atual.estado == Estado.ERRO)
                && agora - atual.ultimaAtualizacao > VALIDADE_RESULTADO_MS) {
            atual = null; // resultado antigo ja consultado, liberta para nova medicao
        }
    }

    // avaliacao com os limites personalizados do paciente, ou os padrao;
    // fica guardada no registo porque os limites podem mudar no futuro
    private String avaliar(Long idPaciente, Double temperatura, Integer bpm) {
        ValoresReferenciaPaciente referencia =
                valoresReferenciaRepository.findTopByIdPacienteOrderByDataDefinicaoDesc(idPaciente);

        List<String> alertas = new ArrayList<>();

        if (temperatura != null) {
            double maximo = (referencia != null && referencia.getTemperaturaMaxima() != null)
                    ? referencia.getTemperaturaMaxima() : TEMPERATURA_MAXIMA_PADRAO;
            atual.temperaturaMaxima = maximo;
            if (temperatura > maximo) {
                alertas.add("FEBRE");
            }
        }

        if (bpm != null) {
            // prioridade: valores do medico > referencia por idade > padrao adulto
            int[] limitesIdade = limitesBpmPorIdade(idPaciente);
            int minimo = (referencia != null && referencia.getBpmMinimo() != null)
                    ? referencia.getBpmMinimo() : limitesIdade[0];
            int maximo = (referencia != null && referencia.getBpmMaximo() != null)
                    ? referencia.getBpmMaximo() : limitesIdade[1];
            atual.bpmMinimo = minimo;
            atual.bpmMaximo = maximo;
            if (bpm < minimo) {
                alertas.add("BPM_BAIXO");
            } else if (bpm > maximo) {
                alertas.add("BPM_ALTO");
            }
        }

        return alertas.isEmpty() ? "NORMAL" : String.join(",", alertas);
    }

    // intervalos de referencia da frequencia cardiaca em repouso por idade:
    // escaloes pediatricos das tabelas PALS, adultos 60-100 da AHA.
    // O sexo nao entra: as referencias clinicas nao diferenciam limites por sexo
    private int[] limitesBpmPorIdade(Long idPaciente) {
        Paciente paciente = pacienteRepository.findById(idPaciente).orElse(null);
        if (paciente == null || paciente.getDataNascimento() == null) {
            return new int[]{BPM_MINIMO_PADRAO, BPM_MAXIMO_PADRAO};
        }

        int idade = java.time.Period.between(paciente.getDataNascimento(), java.time.LocalDate.now()).getYears();
        if (idade < 1) return new int[]{100, 190};
        if (idade <= 2) return new int[]{98, 140};
        if (idade <= 5) return new int[]{80, 120};
        if (idade <= 11) return new int[]{75, 118};
        return new int[]{BPM_MINIMO_PADRAO, BPM_MAXIMO_PADRAO};
    }

    private void enviarRespostaDispositivo(String resultado) {
        JsonObject resposta = new JsonObject();
        resposta.addProperty("tipo", "respostaMedicao");
        resposta.addProperty("idMedicao", atual.idMedicao);
        resposta.addProperty("resultado", resultado);
        try {
            publicarComando(atual.deviceId, resposta);
        } catch (ResponseStatusException e) {
            System.out.println("Aviso: nao foi possivel enviar feedback ao dispositivo");
        }
    }

    private void publicarComando(String deviceId, JsonObject dados) {
        try {
            mqttConfig.publicarComando(deviceId, MqttEnvelopeUtil.montarEnvelopeEncriptado(dados.toString()));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao comunicar com o dispositivo.");
        }
    }

    private Map<String, Object> estadoComoMapa(MedicaoEmCurso medicao) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("idMedicao", medicao.idMedicao);
        mapa.put("tipo", medicao.tipo);
        mapa.put("estado", medicao.estado);
        mapa.put("fase", medicao.fase);
        mapa.put("valorParcial", medicao.valorParcial);
        mapa.put("dedo", medicao.dedo);
        mapa.put("progresso", medicao.progresso);
        mapa.put("validos", medicao.validos);
        mapa.put("alvo", medicao.alvo);

        if (medicao.estado == Estado.ERRO) {
            mapa.put("motivo", medicao.motivoErro);
        }
        if (medicao.estado == Estado.CONCLUIDA) {
            Map<String, Object> resultado = new LinkedHashMap<>();
            resultado.put("temperatura", medicao.temperatura);
            resultado.put("bpm", medicao.bpm);
            resultado.put("avaliacao", medicao.avaliacao);
            resultado.put("temperaturaMaxima", medicao.temperaturaMaxima);
            resultado.put("bpmMinimo", medicao.bpmMinimo);
            resultado.put("bpmMaximo", medicao.bpmMaximo);
            mapa.put("resultado", resultado);
        }
        return mapa;
    }
}
