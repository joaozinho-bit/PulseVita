#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include "mbedtls/aes.h"
#include "mbedtls/base64.h"

//===== pinos do ESP32 =====
const int pinLed = 2; // LED integrado do ESP32
const int pinBuzzer = 15; // pino do buzzer

// Estados possíveis do feedback (LEDs, buzzer, etc.) do dispositivo
enum EstadoFeedback { ESTADO_INATIVO, ESTADO_SUCESSO, ESTADO_ERRO, ESTADO_CARREGANDO };
EstadoFeedback estadoAtual = ESTADO_INATIVO;

// Variáveis para controlar o tempo de cada estado e o piscar do LED
unsigned long inicioEstado = 0;
unsigned long ultimoPiscar = 0;
bool piscarLigado = false;

void definirSucesso() {
  estadoAtual = ESTADO_SUCESSO;
  inicioEstado = millis();
  tone(pinBuzzer, 1000, 200);
}

void definirErro() {
  estadoAtual = ESTADO_ERRO;
  inicioEstado = millis();
  tone(pinBuzzer, 300, 500);
}

void definirCarregando() {
  estadoAtual = ESTADO_CARREGANDO;
  inicioEstado = millis();
}

// chamada a cada iteração do loop(), nunca bloqueia porque usa millis() para controlar o tempo
void atualizarFeedback() {
  unsigned long agora = millis();

  switch (estadoAtual) {
    case ESTADO_SUCESSO:
      definirCorLed(0, 255, 0);
      if (agora - inicioEstado > 1500) { 
        apagarLed(); estadoAtual = ESTADO_INATIVO; 
      }
      break;

    case ESTADO_ERRO:
      definirCorLed(255, 0, 0);
      if (agora - inicioEstado > 1500) { 
        apagarLed(); estadoAtual = ESTADO_INATIVO; 
      }
      break;

    case ESTADO_CARREGANDO:
      // pisca amarelo a cada 300ms
      if (agora - ultimoPiscar > 300) {
        piscarLigado = !piscarLigado;
        ultimoPiscar = agora;
        if (piscarLigado) {
          definirCorLed(255, 255, 0);
        } else {
          apagarLed();
        }
      }
      break;

    // estado inativo, não faz nada
    case ESTADO_INATIVO:
      break;
  }
}

// envia o MAC address ao backend em resposta a um pedido "pedirMac"
// usa o mesmo envelope AES+CRC32 de todas as outras mensagens
void enviarMacAddress() {
  String mac = WiFi.macAddress();

  StaticJsonDocument<100> dados;
  dados["idDispositivo"] = "PV-X7K2M9";
  dados["macAddress"] = mac;

  String dadosJson;
  serializeJson(dados, dadosJson);

  uint32_t crc32 = calcularCrc32(dadosJson);

  StaticJsonDocument<200> envelope;
  envelope["dados"] = dadosJson;
  envelope["crc32"] = crc32;

  String envelopeJson;
  serializeJson(envelope, envelopeJson);

  String envelopeComPadding = aplicarPadding(envelopeJson);
  String mensagemEncriptada = encriptarEConverter(envelopeComPadding);

  client.publish("pulsevita/registo", mensagemEncriptada.c_str());
  Serial.println("MAC enviado: " + mac);
}

// verifica se a mensagem recebida é um pedido imediato e despacha sem esperar o loop
// pedirMac é tratado aqui porque a resposta tem de ser enviada assim que o pedido chega
void processarComandoImediato(const String& mensagemBase64) {
  String envelopeJson = desencriptarEConverter(mensagemBase64);
  if (envelopeJson.isEmpty()) return;

  StaticJsonDocument<300> envelope;
  if (deserializeJson(envelope, envelopeJson) != DeserializationError::Ok) return;

  String dados = envelope["dados"].as<String>();

  StaticJsonDocument<100> json;
  if (deserializeJson(json, dados) != DeserializationError::Ok) return;

  String tipo = json["tipo"].as<String>();

  if (tipo == "pedirMac") {
    enviarMacAddress();
  } else if (tipo == "pedirLeitura") {
    enviarMensagemTeste();
  }
}

// ===== configuracao de rede =====
// Casa
//const char* ssidWifi = "NOS-BB34";
//const char* passwordWifi = "RY6FRT3G";
// const char* brokerMqtt = "192.168.1.10";

// Telemóvel
const char* ssidWifi = "OPPO Reno14 5G B4C6";
const char* passwordWifi = "spsu8597";

const char* brokerMqtt = "10.169.110.231";
const int portaMqtt = 1883;
const char* topicoLeituras = "pulsevita/leituras";
const char* topicoComandos = "pulsevita/comandos/PV-X7K2M9";

// chave AES-128 de 16 bytes, tem de ser EXATAMENTE igual no Spring Boot
const uint8_t chaveAes[16] = {
  'P','u','l','s','e','V','i','t','a','A','E','S','1','2','3','!'
};

WiFiClient wifiClient;
PubSubClient client(wifiClient);

// guarda a ultima mensagem recebida do backend, para so ser impressa no log periodico
String ultimaMensagemRecebida = "";
bool novaMensagemDisponivel = false;

// calcula CRC32 de uma string, mesma logica que o Java vai usar para validar
uint32_t calcularCrc32(const String& texto) {
  uint32_t crc = 0xFFFFFFFF;
  for (size_t i = 0; i < texto.length(); i++) {
    uint8_t byte = texto[i];
    crc ^= byte;
    for (int j = 0; j < 8; j++) {
      crc = (crc >> 1) ^ (0xEDB88320 & (-(crc & 1)));
    }
  }
  return ~crc;
}

// aplica padding PKCS7 para o tamanho ser multiplo de 16 bytes (exigido pelo AES)
String aplicarPadding(const String& texto) {
  int resto = texto.length() % 16;
  int padding = 16 - resto;
  String resultado = texto;
  for (int i = 0; i < padding; i++) {
    resultado += (char)padding;
  }
  return resultado;
}

// remove o padding PKCS7, lendo o ultimo byte para saber quantos remover
String removerPadding(const String& texto) {
  if (texto.length() == 0) return texto;
  int padding = (uint8_t)texto[texto.length() - 1];
  if (padding > 0 && padding <= 16 && padding <= texto.length()) {
    return texto.substring(0, texto.length() - padding);
  }
  return texto;
}

// encripta com AES-128 em modo ECB e devolve o resultado em Base64
String encriptarEConverter(const String& textoComPadding) {
  mbedtls_aes_context ctx;
  mbedtls_aes_init(&ctx);

  int resultadoKey = mbedtls_aes_setkey_enc(&ctx, chaveAes, 128);
  if (resultadoKey != 0) {
    Serial.println("ERRO ao definir a chave AES: " + String(resultadoKey));
    mbedtls_aes_free(&ctx);
    return "";
  }

  size_t tamanho = textoComPadding.length();
  uint8_t entrada[256];
  uint8_t saida[300];
  textoComPadding.getBytes(entrada, tamanho + 1);

  for (size_t i = 0; i < tamanho; i += 16) {
    mbedtls_aes_crypt_ecb(&ctx, MBEDTLS_AES_ENCRYPT, entrada + i, saida + i);
  }

  mbedtls_aes_free(&ctx);

  unsigned char base64Saida[400];
  size_t base64Tamanho;
  mbedtls_base64_encode(base64Saida, sizeof(base64Saida), &base64Tamanho, saida, tamanho);
  base64Saida[base64Tamanho] = '\0';

  return String((char*)base64Saida);
}

// desencripta uma mensagem recebida em Base64, devolve o texto original sem padding
String desencriptarEConverter(const String& base64Recebido) {
  unsigned char dadosEncriptados[300];
  size_t tamanhoDecodificado;

  int resultadoDecode = mbedtls_base64_decode(
    dadosEncriptados, sizeof(dadosEncriptados), &tamanhoDecodificado,
    (const unsigned char*)base64Recebido.c_str(), base64Recebido.length()
  );

  if (resultadoDecode != 0) {
    Serial.println("ERRO ao decodificar Base64: " + String(resultadoDecode));
    return "";
  }

  mbedtls_aes_context ctx;
  mbedtls_aes_init(&ctx);

  int resultadoKey = mbedtls_aes_setkey_dec(&ctx, chaveAes, 128);
  if (resultadoKey != 0) {
    Serial.println("ERRO ao definir a chave AES (decrypt): " + String(resultadoKey));
    mbedtls_aes_free(&ctx);
    return "";
  }

  uint8_t saida[300];
  for (size_t i = 0; i < tamanhoDecodificado; i += 16) {
    mbedtls_aes_crypt_ecb(&ctx, MBEDTLS_AES_DECRYPT, dadosEncriptados + i, saida + i);
  }

  mbedtls_aes_free(&ctx);

  String textoComPadding = String((char*)saida).substring(0, tamanhoDecodificado);
  return removerPadding(textoComPadding);
}

void conectarWifi() {
  WiFi.begin(ssidWifi, passwordWifi);
  Serial.print("A ligar ao WiFi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWiFi ligado, IP: " + WiFi.localIP().toString());
}

// chamada automaticamente sempre que chega uma mensagem num topico subscrito
// nao imprime nada aqui, so guarda o estado, para o log do loop controlar a cadencia
void aoReceberMensagem(char* topico, byte* payload, unsigned int tamanho) {
  String mensagem;
  for (unsigned int i = 0; i < tamanho; i++) {
    mensagem += (char)payload[i];
  }

  // comandos que precisam de resposta imediata são despachados aqui,
  // sem esperar pelo bloco de log periódico do loop
  if (String(topico) == topicoComandos) {
    processarComandoImediato(mensagem);
  }

  ultimaMensagemRecebida = mensagem;
  novaMensagemDisponivel = true;
}

void conectarMqtt() {
  client.setServer(brokerMqtt, portaMqtt);
  client.setCallback(aoReceberMensagem);

  while (!client.connected()) {
    Serial.print("A ligar ao broker MQTT...");
    if (client.connect("ESP32-PulseVita")) {
      Serial.println("ligado");
      client.subscribe(topicoComandos);
    } else {
      Serial.println("falhou, a tentar de novo em 2s");
      delay(2000);
    }
  }
}

void enviarMensagemTeste() {
  StaticJsonDocument<200> dados;
  dados["deviceId"] = "PV-X7K2M9";
  dados["valor"] = 36.8;
  dados["timestamp"] = millis();

  String dadosJson;
  serializeJson(dados, dadosJson);

  uint32_t crc32 = calcularCrc32(dadosJson);

  StaticJsonDocument<300> envelope;
  envelope["dados"] = dadosJson;
  envelope["crc32"] = crc32;

  String envelopeJson;
  serializeJson(envelope, envelopeJson);

  String envelopeComPadding = aplicarPadding(envelopeJson);
  String mensagemEncriptada = encriptarEConverter(envelopeComPadding);

  Serial.println("JSON original: " + dadosJson);
  Serial.println("CRC32: " + String(crc32));
  Serial.println("Tamanho da mensagem: " + String(mensagemEncriptada.length()));

  bool sucesso = client.publish(topicoLeituras, mensagemEncriptada.c_str());
  Serial.println("Publish sucesso: " + String(sucesso));
}

void setup() {
  Serial.begin(115200);
  conectarWifi();
  conectarMqtt();
}

void loop() {
  if (!client.connected()) {
    conectarMqtt();
  }
  client.loop(); // processa publish e callbacks em segundo plano, continuamente

  // o log e o envio so acontecem a cada 5 segundos, para nao encher o Serial Monitor
  static unsigned long ultimoLog = 0;
  if (millis() - ultimoLog > 5000) {
    Serial.println("--- ESP32: a enviar dados ---");
    enviarMensagemTeste();

    if (novaMensagemDisponivel) {
      Serial.println("--- ESP32: mensagem recebida do backend ---");
      String comandoDesencriptado = desencriptarEConverter(ultimaMensagemRecebida);
      Serial.println(comandoDesencriptado);
      novaMensagemDisponivel = false;
    } else {
      Serial.println("--- ESP32: nenhuma mensagem nova do backend ---");
    }

    ultimoLog = millis();
  }
}