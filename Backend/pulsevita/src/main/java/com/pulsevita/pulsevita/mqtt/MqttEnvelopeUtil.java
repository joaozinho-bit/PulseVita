package com.pulsevita.pulsevita.mqtt;

import com.google.gson.JsonObject;
import com.pulsevita.pulsevita.crypto.AesUtil;

import java.util.zip.CRC32;

// Monta o envelope {dados, crc32} usado em todas as mensagens para o ESP32,
// ja encriptado com AES e codificado em Base64
public class MqttEnvelopeUtil {

    public static String montarEnvelopeEncriptado(String dadosJson) throws Exception {
        CRC32 crc = new CRC32();
        crc.update(dadosJson.getBytes());

        JsonObject envelope = new JsonObject();
        envelope.addProperty("dados", dadosJson);
        envelope.addProperty("crc32", crc.getValue());

        return AesUtil.encriptar(envelope.toString());
    }
}
