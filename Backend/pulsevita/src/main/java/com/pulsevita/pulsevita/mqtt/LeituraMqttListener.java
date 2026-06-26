package com.pulsevita.pulsevita.mqtt;

import com.pulsevita.pulsevita.crypto.AesUtil;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Component;
import java.util.zip.CRC32;

@Component
public class LeituraMqttListener {

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

            System.out.println("Dados validos recebidos:");
            System.out.println("  deviceId: " + dados.get("deviceId").getAsString());
            System.out.println("  valor: " + dados.get("valor").getAsDouble());

            // aqui entra a chamada ao service que ja existe, para gravar na BD

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