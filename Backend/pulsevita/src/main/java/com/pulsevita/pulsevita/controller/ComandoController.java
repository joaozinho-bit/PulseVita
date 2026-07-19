package com.pulsevita.pulsevita.controller;

import com.pulsevita.pulsevita.mqtt.MqttConfig;
import com.pulsevita.pulsevita.mqtt.MqttEnvelopeUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ComandoController {

    private final MqttConfig mqttConfig;

    public ComandoController(MqttConfig mqttConfig) {
        this.mqttConfig = mqttConfig;
    }

    @PostMapping("/dispositivos/{deviceId}/pedir-leitura")
    public String pedirLeitura(@PathVariable String deviceId) {
        try {
            String dados = "{\"tipo\":\"pedirLeitura\"}";
            String mensagemEncriptada = MqttEnvelopeUtil.montarEnvelopeEncriptado(dados);
            mqttConfig.publicarComando(deviceId, mensagemEncriptada);
            return "Comando enviado para o dispositivo " + deviceId;
        } catch (Exception e) {
            return "Erro ao enviar comando: " + e.getMessage();
        }
    }
}