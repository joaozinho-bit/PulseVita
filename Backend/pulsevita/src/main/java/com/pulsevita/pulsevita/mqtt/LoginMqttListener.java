package com.pulsevita.pulsevita.mqtt;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pulsevita.pulsevita.crypto.AesUtil;
import com.pulsevita.pulsevita.model.Medico;
import com.pulsevita.pulsevita.service.LoginCartaoService;
import com.pulsevita.pulsevita.service.MedicoService;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.zip.CRC32;

@Component
public class LoginMqttListener {

    private final MedicoService medicoService;
    private final LoginCartaoService loginCartaoService;
    private final MqttConfig mqttConfig;

    // @Lazy quebra a dependencia circular: MqttConfig precisa deste listener
    // para subscrever o topico, e este listener precisa do MqttConfig para responder
    public LoginMqttListener(MedicoService medicoService,
                             LoginCartaoService loginCartaoService,
                             @Lazy MqttConfig mqttConfig) {
        this.medicoService = medicoService;
        this.loginCartaoService = loginCartaoService;
        this.mqttConfig = mqttConfig;
    }

    public void processarMensagem(String topic, MqttMessage msg) {
        try {
            String base64Recebido = new String(msg.getPayload());
            String envelopeJson = AesUtil.desencriptar(base64Recebido);

            JsonObject envelope = JsonParser.parseString(envelopeJson).getAsJsonObject();
            String dadosJson = envelope.get("dados").getAsString();
            long crc32Recebido = envelope.get("crc32").getAsLong();

            if (calcularCrc32(dadosJson) != crc32Recebido) {
                System.out.println("ERRO: CRC32 invalido na mensagem de login");
                return;
            }

            JsonObject dados = JsonParser.parseString(dadosJson).getAsJsonObject();
            String deviceId = dados.get("deviceId").getAsString();
            String idCartao = dados.get("idCartao").getAsString();

            Medico medico = medicoService.loginPorCartao(idCartao);

            if (medico != null) {
                loginCartaoService.registarLoginPendente(medico);
                System.out.println("Login por cartao valido: " + medico.getNome());
            } else {
                loginCartaoService.registarCartaoInvalido();
                System.out.println("Login por cartao rejeitado, UID desconhecido: " + idCartao);
            }

            enviarResposta(deviceId, medico != null);

        } catch (Exception e) {
            System.out.println("Erro ao processar login por cartao: " + e.getMessage());
        }
    }

    private void enviarResposta(String deviceId, boolean sucesso) throws Exception {
        JsonObject resposta = new JsonObject();
        resposta.addProperty("tipo", "respostaLogin");
        resposta.addProperty("resultado", sucesso ? "sucesso" : "erro");

        String mensagem = MqttEnvelopeUtil.montarEnvelopeEncriptado(resposta.toString());
        mqttConfig.publicarComando(deviceId, mensagem);
    }

    private long calcularCrc32(String texto) {
        CRC32 crc = new CRC32();
        crc.update(texto.getBytes());
        return crc.getValue();
    }
}
