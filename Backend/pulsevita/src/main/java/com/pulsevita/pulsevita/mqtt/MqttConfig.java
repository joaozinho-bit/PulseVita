package com.pulsevita.pulsevita.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@Configuration
public class MqttConfig {

    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Value("${mqtt.client.id}")
    private String clientId;

    @Value("${mqtt.topic.leituras}")
    private String topicoLeituras;

    @Value("${mqtt.topic.login}")
    private String topicoLogin;

    private MqttClient client;
    private final LeituraMqttListener leituraListener;
    private final LoginMqttListener loginListener;

    public MqttConfig(LeituraMqttListener leituraListener, LoginMqttListener loginListener) {
        this.leituraListener = leituraListener;
        this.loginListener = loginListener;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void conectarMqtt() {
        try {
            client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
            client.connect();
            client.subscribe(topicoLeituras, (topic, msg) -> leituraListener.processarMensagem(topic, msg));
            client.subscribe(topicoLogin, (topic, msg) -> loginListener.processarMensagem(topic, msg));
            System.out.println("MQTT ligado e a escuta nos topicos: " + topicoLeituras + ", " + topicoLogin);
        } catch (MqttException e) {
            System.out.println("Erro ao ligar ao MQTT: " + e.getMessage());
        }
    }

    public void publicarComando(String deviceId, String mensagemEncriptada) {
        try {
            String topico = "pulsevita/comandos/" + deviceId;
            MqttMessage mensagem = new MqttMessage(mensagemEncriptada.getBytes());
            mensagem.setQos(1);
            client.publish(topico, mensagem);
            System.out.println("Comando publicado em " + topico);
        } catch (MqttException e) {
            System.out.println("Erro ao publicar comando: " + e.getMessage());
        }
    }
}