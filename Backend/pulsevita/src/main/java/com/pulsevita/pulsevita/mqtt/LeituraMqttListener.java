package com.pulsevita.pulsevita.mqtt;

import com.pulsevita.pulsevita.crypto.AesUtil;
import com.pulsevita.pulsevita.service.MedicaoService;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Component;
import java.util.zip.CRC32;

@Component
public class LeituraMqttListener {

    private final MedicaoService medicaoService;

    public LeituraMqttListener(MedicaoService medicaoService) {
        this.medicaoService = medicaoService;
    }

    public void processarMensagem(String topic, MqttMessage msg) {
        try {
            String base64Recebido = new String(msg.getPayload());
            String envelopeJson = AesUtil.desencriptar(base64Recebido);

            JsonObject envelope = JsonParser.parseString(envelopeJson).getAsJsonObject();
            String dadosJson = envelope.get("dados").getAsString();
            long crc32Recebido = envelope.get("crc32").getAsLong();

            long crc32Calculado = calcularCrc32(dadosJson);
            if (crc32Calculado != crc32Recebido) {
                System.out.println("ERRO: CRC32 invalido, mensagem corrompida");
                return;
            }

            JsonObject dados = JsonParser.parseString(dadosJson).getAsJsonObject();

            // as mensagens do fluxo de medicao trazem um campo "tipo";
            // as antigas de teste ({deviceId, valor}) nao, e caem no ramo final
            if (dados.has("tipo")) {
                String tipo = dados.get("tipo").getAsString();
                long idMedicao = dados.has("idMedicao") ? dados.get("idMedicao").getAsLong() : 0;

                if ("estadoMedicao".equals(tipo)) {
                    medicaoService.registarEstadoDispositivo(idMedicao, dados.get("estado").getAsString());
                } else if ("resultadoMedicao".equals(tipo)) {
                    Double temperatura = dados.has("temperatura") && !dados.get("temperatura").isJsonNull()
                            ? dados.get("temperatura").getAsDouble() : null;
                    Integer bpm = dados.has("bpm") && !dados.get("bpm").isJsonNull()
                            ? dados.get("bpm").getAsInt() : null;
                    medicaoService.registarResultado(idMedicao, temperatura, bpm);
                } else if ("progressoMedicao".equals(tipo)) {
                    String fase = dados.has("fase") ? dados.get("fase").getAsString() : null;
                    Integer bpm = dados.has("bpm") && !dados.get("bpm").isJsonNull()
                            ? dados.get("bpm").getAsInt() : null;
                    Boolean dedo = dados.has("dedo") ? dados.get("dedo").getAsBoolean() : null;
                    Integer progresso = dados.has("progresso") ? dados.get("progresso").getAsInt() : null;
                    Integer validos = dados.has("validos") ? dados.get("validos").getAsInt() : null;
                    Integer alvo = dados.has("alvo") ? dados.get("alvo").getAsInt() : null;
                    medicaoService.registarProgresso(idMedicao, fase, bpm, dedo, progresso, validos, alvo);
                } else {
                    System.out.println("Mensagem de leitura com tipo desconhecido: " + tipo);
                }
                return;
            }

            System.out.println("Dados validos recebidos:");
            System.out.println("  deviceId: " + dados.get("deviceId").getAsString());
            System.out.println("  valor: " + dados.get("valor").getAsDouble());

        } catch (Exception e) {
            System.out.println("Erro ao processar mensagem: " + e.getMessage());
        }
    }

    private long calcularCrc32(String texto) {
        CRC32 crc = new CRC32();
        crc.update(texto.getBytes());
        return crc.getValue();
    }
}
