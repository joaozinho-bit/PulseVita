#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include <SPI.h>
#include <MFRC522.h>
#include <Wire.h>
#include <Adafruit_MLX90614.h>
#include "mbedtls/aes.h"
#include "mbedtls/base64.h"

//===== pinos do ESP32 =====
const int pinBuzzer = 25;
const int pinLedR = 27;
const int pinLedG = 26;
const int pinLedB = 32;
const int pinRfidSS = 5;   // NSS do RC522
const int pinRfidRST = 4;  // RST do RC522
const int pinBotao = 33;   // botao ligar/desligar (RTC GPIO, acorda do deep sleep)
// SPI do RC522 usa os pinos por defeito: SCK=18, MISO=19, MOSI=23
// reservados para os proximos passos: I2C temperatura SDA=21/SCL=22,
// sensor de pulsacao S=34 (ADC1)

MFRC522 rfid(pinRfidSS, pinRfidRST);

// sensor de temperatura por infravermelhos no I2C (SDA=21, SCL=22)
Adafruit_MLX90614 sensorTemp = Adafruit_MLX90614();
bool sensorTempOk = false;

// offset de calibracao afinado no sketch TesteTemperatura: um sensor IV le a
// temperatura da pele, 2 a 3 graus abaixo da corporal
const float offsetCalibracao = 2.5;

// sessao de medicao: amostra a cada 200ms ate juntar 20 amostras validas (~4s)
const int amostrasPorMedicao = 20;
const unsigned long intervaloAmostraMs = 200;
const unsigned long timeoutMedicaoMs = 10000;

bool medicaoEmCurso = false;
long idMedicaoAtual = 0;
unsigned long inicioMedicao = 0;
unsigned long ultimaAmostra = 0;
int amostrasRecolhidas = 0;
float somaTemperatura = 0;

// depois de enviar o resultado, aguarda a avaliacao do backend para o feedback
bool aguardaRespostaMedicao = false;
unsigned long inicioEsperaRespostaMedicao = 0;

// ===== configuracao de rede =====
// Casa
const char* ssidWifi = "NOS-BB34";
const char* passwordWifi = "RY6FRT3G";
const char* brokerMqtt = "192.168.1.10";

// Telemóvel
// const char* ssidWifi = "OPPO Reno14 5G B4C6";
// const char* passwordWifi = "spsu8597";
// const char* brokerMqtt = "10.169.110.231";

const int portaMqtt = 1883;
const char* topicoLeituras = "pulsevita/leituras";
const char* topicoComandos = "pulsevita/comandos/PV-X7K2M9";
const char* topicoLogin = "pulsevita/login";

// chave AES-128 de 16 bytes, tem de ser EXATAMENTE igual no Spring Boot
const uint8_t chaveAes[16] = {
  'P','u','l','s','e','V','i','t','a','A','E','S','1','2','3','!'
};

WiFiClient wifiClient;
PubSubClient client(wifiClient);

// guarda a ultima mensagem recebida do backend, para so ser impressa no log periodico
String ultimaMensagemRecebida = "";
bool novaMensagemDisponivel = false;

// Estados possíveis do feedback (LEDs, buzzer, etc.) do dispositivo
enum EstadoFeedback { ESTADO_INATIVO, ESTADO_SUCESSO, ESTADO_ERRO, ESTADO_CARREGANDO };
EstadoFeedback estadoAtual = ESTADO_INATIVO;

// Variáveis para controlar o tempo de cada estado e o piscar do LED
unsigned long inicioEstado = 0;
unsigned long ultimoPiscar = 0;
bool piscarLigado = false;

// controlo da leitura de cartoes e da espera pela resposta do backend
unsigned long ultimaLeituraCartao = 0;
bool aguardaRespostaLogin = false;
unsigned long inicioEsperaLogin = 0;

// gestao de energia: o botao desliga (deep sleep) e a inatividade prolongada tambem
// 30 min durante o desenvolvimento para nao adormecer a meio dos testes; repor 5 min na versao final
const unsigned long tempoMaxInatividade = 30UL * 60UL * 1000UL;
unsigned long ultimaAtividade = 0;
volatile bool botaoPremido = false;
volatile unsigned long ultimoToqueBotao = 0;

void IRAM_ATTR aoPremirBotao() {
  unsigned long agora = millis();
  if (agora - ultimoToqueBotao > 300) { // debounce
    botaoPremido = true;
    ultimoToqueBotao = agora;
  }
}

// LED RGB de catodo comum, cada canal simplesmente ligado ou desligado
// digitalWrite em vez de PWM para nao partilhar canais LEDC com o tone() do buzzer
void definirCorLed(int r, int g, int b) {
  digitalWrite(pinLedR, r > 0 ? HIGH : LOW);
  digitalWrite(pinLedG, g > 0 ? HIGH : LOW);
  digitalWrite(pinLedB, b > 0 ? HIGH : LOW);
}

void apagarLed() {
  definirCorLed(0, 0, 0);
}

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
  // o piscar comeca sempre na fase acesa, para o feedback ser consistente
  piscarLigado = true;
  ultimoPiscar = millis();
  definirCorLed(0, 0, 255);
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
      // pisca azul a cada 300ms
      if (agora - ultimoPiscar > 300) {
        piscarLigado = !piscarLigado;
        ultimoPiscar = agora;
        if (piscarLigado) {
          definirCorLed(0, 0, 255);
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

// verifica se ha um cartao novo no leitor e envia o pedido de login ao backend
// cooldown de 3s para nao processar o mesmo cartao varias vezes seguidas
void verificarCartao() {
  if (millis() - ultimaLeituraCartao < 3000) return;
  if (!rfid.PICC_IsNewCardPresent() || !rfid.PICC_ReadCardSerial()) return;

  String uid = "";
  for (byte i = 0; i < rfid.uid.size; i++) {
    if (rfid.uid.uidByte[i] < 0x10) uid += "0";
    uid += String(rfid.uid.uidByte[i], HEX);
  }
  uid.toUpperCase();

  rfid.PICC_HaltA();
  rfid.PCD_StopCrypto1();

  ultimaLeituraCartao = millis();
  ultimaAtividade = millis();
  Serial.println("Cartao lido, UID: " + uid);
  enviarLoginCartao(uid);
}

// envia o UID ao backend no envelope AES+CRC32 habitual e fica a aguardar resposta
void enviarLoginCartao(const String& uid) {
  StaticJsonDocument<150> dados;
  dados["deviceId"] = "PV-X7K2M9";
  dados["idCartao"] = uid;
  dados["timestamp"] = millis();

  String dadosJson;
  serializeJson(dados, dadosJson);

  uint32_t crc32 = calcularCrc32(dadosJson);

  StaticJsonDocument<250> envelope;
  envelope["dados"] = dadosJson;
  envelope["crc32"] = crc32;

  String envelopeJson;
  serializeJson(envelope, envelopeJson);

  String envelopeComPadding = aplicarPadding(envelopeJson);
  String mensagemEncriptada = encriptarEConverter(envelopeComPadding);

  client.publish(topicoLogin, mensagemEncriptada.c_str());

  definirCarregando();
  aguardaRespostaLogin = true;
  inicioEsperaLogin = millis();
}

// publica no envelope habitual uma mensagem de estado da medicao
// ("iniciada" quando o comando e aceite, "erro" quando nao e possivel medir)
void enviarEstadoMedicao(const char* estado) {
  StaticJsonDocument<200> dados;
  dados["deviceId"] = "PV-X7K2M9";
  dados["tipo"] = "estadoMedicao";
  dados["idMedicao"] = idMedicaoAtual;
  dados["estado"] = estado;

  String dadosJson;
  serializeJson(dados, dadosJson);

  uint32_t crc32 = calcularCrc32(dadosJson);

  StaticJsonDocument<300> envelope;
  envelope["dados"] = dadosJson;
  envelope["crc32"] = crc32;

  String envelopeJson;
  serializeJson(envelope, envelopeJson);

  String mensagemEncriptada = encriptarEConverter(aplicarPadding(envelopeJson));
  client.publish(topicoLeituras, mensagemEncriptada.c_str());
}

void enviarResultadoMedicao(float temperatura) {
  StaticJsonDocument<200> dados;
  dados["deviceId"] = "PV-X7K2M9";
  dados["tipo"] = "resultadoMedicao";
  dados["idMedicao"] = idMedicaoAtual;
  dados["temperatura"] = ((int)(temperatura * 100)) / 100.0;

  String dadosJson;
  serializeJson(dados, dadosJson);

  uint32_t crc32 = calcularCrc32(dadosJson);

  StaticJsonDocument<300> envelope;
  envelope["dados"] = dadosJson;
  envelope["crc32"] = crc32;

  String envelopeJson;
  serializeJson(envelope, envelopeJson);

  String mensagemEncriptada = encriptarEConverter(aplicarPadding(envelopeJson));
  client.publish(topicoLeituras, mensagemEncriptada.c_str());

  Serial.println("Resultado da medicao enviado: " + String(temperatura, 2) + " C");
}

// arranca uma sessao de medicao de temperatura pedida pelo backend
void iniciarSessaoMedicao(long idMedicao) {
  idMedicaoAtual = idMedicao;

  if (!sensorTempOk || medicaoEmCurso) {
    enviarEstadoMedicao("erro");
    definirErro();
    return;
  }

  medicaoEmCurso = true;
  inicioMedicao = millis();
  ultimaAmostra = 0;
  amostrasRecolhidas = 0;
  somaTemperatura = 0;

  definirCarregando();
  enviarEstadoMedicao("iniciada");
  Serial.println("Medicao de temperatura iniciada, id: " + String(idMedicao));
}

void cancelarSessaoMedicao() {
  if (!medicaoEmCurso) return;
  medicaoEmCurso = false;
  apagarLed();
  estadoAtual = ESTADO_INATIVO;
  Serial.println("Medicao cancelada pelo backend");
}

// chamada em cada iteracao do loop; nao bloqueia, amostra por millis()
void processarMedicao() {
  if (!medicaoEmCurso) return;

  // sensor sempre a falhar (ex.: desligado a meio): desiste e avisa o backend
  if (millis() - inicioMedicao > timeoutMedicaoMs) {
    medicaoEmCurso = false;
    enviarEstadoMedicao("erro");
    definirErro();
    return;
  }

  if (millis() - ultimaAmostra < intervaloAmostraMs) return;
  ultimaAmostra = millis();

  float leitura = sensorTemp.readObjectTempC();
  if (isnan(leitura)) return; // amostra falhada, tenta na proxima

  somaTemperatura += leitura + offsetCalibracao;
  amostrasRecolhidas++;
  ultimaAtividade = millis(); // medir conta como atividade para o auto-sleep

  if (amostrasRecolhidas >= amostrasPorMedicao) {
    medicaoEmCurso = false;
    enviarResultadoMedicao(somaTemperatura / amostrasRecolhidas);
    // o LED mantem-se a piscar ate chegar a respostaMedicao com a avaliacao
    aguardaRespostaMedicao = true;
    inicioEsperaRespostaMedicao = millis();
  }
}

// verifica se a mensagem recebida é um pedido imediato e despacha sem esperar o loop
// pedirMac é tratado aqui porque a resposta tem de ser enviada assim que o pedido chega
void processarComandoImediato(const String& mensagemBase64) {
  String envelopeJson = desencriptarEConverter(mensagemBase64);
  if (envelopeJson.isEmpty()) return;

  StaticJsonDocument<300> envelope;
  if (deserializeJson(envelope, envelopeJson) != DeserializationError::Ok) return;

  String dados = envelope["dados"].as<String>();

  StaticJsonDocument<200> json;
  if (deserializeJson(json, dados) != DeserializationError::Ok) return;

  String tipo = json["tipo"].as<String>();

  if (tipo == "pedirMac") {
    enviarMacAddress();
  } else if (tipo == "pedirLeitura") {
    enviarMensagemTeste();
  } else if (tipo == "respostaLogin") {
    aguardaRespostaLogin = false;
    String resultado = json["resultado"].as<String>();
    if (resultado == "sucesso") {
      definirSucesso();
    } else {
      definirErro();
    }
  } else if (tipo == "iniciarMedicao") {
    iniciarSessaoMedicao(json["idMedicao"].as<long>());
  } else if (tipo == "pararMedicao") {
    cancelarSessaoMedicao();
  } else if (tipo == "respostaMedicao") {
    aguardaRespostaMedicao = false;
    String resultado = json["resultado"].as<String>();
    if (resultado == "NORMAL") {
      definirSucesso();
    } else {
      definirErro(); // FEBRE ou leitura invalida: feedback de alerta
    }
  }
}

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
  ultimaAtividade = millis(); // contacto do backend conta como atividade

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

// desliga o dispositivo: feedback sonoro, desconexao limpa e deep sleep
// no deep sleep o consumo do chip cai para a ordem dos 10uA e so o botao (ext0) o acorda
void entrarDeepSleep(const char* motivo) {
  Serial.println(String("A entrar em deep sleep, motivo: ") + motivo);
  Serial.println("Premir o botao para voltar a ligar");

  apagarLed();
  tone(pinBuzzer, 800, 150);
  delay(200);
  tone(pinBuzzer, 500, 150);
  delay(200);
  tone(pinBuzzer, 300, 250);
  delay(300);

  client.disconnect();
  WiFi.disconnect(true);

  // se o botao ainda estivesse premido, o ext0 acordava o ESP32 logo de seguida
  while (digitalRead(pinBotao) == LOW) {
    delay(10);
  }
  delay(100);

  esp_sleep_enable_ext0_wakeup((gpio_num_t)pinBotao, 0);
  Serial.flush();
  esp_deep_sleep_start();
}

void setup() {
  Serial.begin(115200);

  pinMode(pinBuzzer, OUTPUT);
  pinMode(pinLedR, OUTPUT);
  pinMode(pinLedG, OUTPUT);
  pinMode(pinLedB, OUTPUT);
  apagarLed();

  pinMode(pinBotao, INPUT_PULLUP);

  // distingue arranque normal de acordar do deep sleep, visivel no Serial Monitor
  if (esp_sleep_get_wakeup_cause() == ESP_SLEEP_WAKEUP_EXT0) {
    Serial.println("\nAcordado do deep sleep pelo botao");
    tone(pinBuzzer, 1200, 150);
  } else {
    Serial.println("\nArranque normal");
  }

  attachInterrupt(digitalPinToInterrupt(pinBotao), aoPremirBotao, FALLING);

  SPI.begin();
  rfid.PCD_Init();

  Wire.begin();
  sensorTempOk = sensorTemp.begin();
  if (!sensorTempOk) {
    Serial.println("AVISO: sensor de temperatura nao encontrado no I2C");
  }

  conectarWifi();
  conectarMqtt();

  ultimaAtividade = millis();
}

void loop() {
  if (!client.connected()) {
    conectarMqtt();
  }
  client.loop(); // processa publish e callbacks em segundo plano, continuamente

  atualizarFeedback();
  verificarCartao();
  processarMedicao();

  // se o backend nao responder ao login dentro de 5s, assume erro
  if (aguardaRespostaLogin && millis() - inicioEsperaLogin > 5000) {
    aguardaRespostaLogin = false;
    definirErro();
  }

  // se a avaliacao da medicao nao chegar em 8s, termina o feedback com erro
  if (aguardaRespostaMedicao && millis() - inicioEsperaRespostaMedicao > 8000) {
    aguardaRespostaMedicao = false;
    definirErro();
  }

  // botao premido = desligar; toques nos primeiros 3s apos o arranque sao
  // ignorados, porque podem ser ricochete do toque que acordou o dispositivo
  if (botaoPremido) {
    botaoPremido = false;
    if (millis() > 3000) {
      entrarDeepSleep("botao premido");
    }
  }

  if (millis() - ultimaAtividade > tempoMaxInatividade) {
    entrarDeepSleep("inatividade");
  }

  // imprime mensagens do backend aqui e nao no callback, para nao atrasar o MQTT
  if (novaMensagemDisponivel) {
    novaMensagemDisponivel = false;
    Serial.println("--- ESP32: mensagem recebida do backend ---");
    Serial.println(desencriptarEConverter(ultimaMensagemRecebida));
  }
}