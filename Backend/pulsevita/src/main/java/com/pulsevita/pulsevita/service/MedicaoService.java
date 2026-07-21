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
        Double temperaturaMaxima; // limite usado na avaliacao, mostrado ao utilizador
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
        if (tipo != TipoMedicao.TEMPERATURA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Este tipo de medição ainda não está disponível.");
        }

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

        if (temperatura != null
                && (temperatura < TEMPERATURA_PLAUSIVEL_MINIMA || temperatura > TEMPERATURA_PLAUSIVEL_MAXIMA)) {
            atual.estado = Estado.ERRO;
            atual.motivoErro = "LEITURA_INVALIDA";
            enviarRespostaDispositivo("INVALIDA");
            System.out.println("Medicao rejeitada por implausibilidade: " + temperatura + " C");
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
            int minimo = (referencia != null && referencia.getBpmMinimo() != null)
                    ? referencia.getBpmMinimo() : BPM_MINIMO_PADRAO;
            int maximo = (referencia != null && referencia.getBpmMaximo() != null)
                    ? referencia.getBpmMaximo() : BPM_MAXIMO_PADRAO;
            if (bpm < minimo) {
                alertas.add("BPM_BAIXO");
            } else if (bpm > maximo) {
                alertas.add("BPM_ALTO");
            }
        }

        return alertas.isEmpty() ? "NORMAL" : String.join(",", alertas);
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
        mapa.put("valorParcial", null); // reservado para os BPM em tempo real

        if (medicao.estado == Estado.ERRO) {
            mapa.put("motivo", medicao.motivoErro);
        }
        if (medicao.estado == Estado.CONCLUIDA) {
            Map<String, Object> resultado = new LinkedHashMap<>();
            resultado.put("temperatura", medicao.temperatura);
            resultado.put("bpm", medicao.bpm);
            resultado.put("avaliacao", medicao.avaliacao);
            resultado.put("temperaturaMaxima", medicao.temperaturaMaxima);
            mapa.put("resultado", resultado);
        }
        return mapa;
    }
}
