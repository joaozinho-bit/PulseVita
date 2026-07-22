// Sketch isolado de teste e calibracao do sensor de temperatura (I2C).
// Nao usa WiFi nem MQTT: serve apenas para afinar o offset de calibracao
// e validar a janela de plausibilidade antes de integrar no sketch principal.
//
// Ligacoes: VIN -> 3V3, GND -> GND, SDA -> GPIO 21, SCL -> GPIO 22
// Biblioteca necessaria: "Adafruit MLX90614 Library" (Library Manager)

#include <Wire.h>
#include <Adafruit_MLX90614.h>

// Um sensor de infravermelhos le a temperatura da pele, que e 2 a 3 graus
// inferior a temperatura corporal. Este offset compensa essa diferenca e
// afina-se comparando com um termometro real (ver procedimento no fim).
const float offsetCalibracao = 2.5;

// Janela de plausibilidade prevista para o valor compensado (a validar aqui):
// abaixo de 33.0 ou acima de 42.5 sera tratado como leitura invalida no backend
const float plausivelMinimo = 33.0;
const float plausivelMaximo = 42.5;

Adafruit_MLX90614 sensorTemp = Adafruit_MLX90614();

// media movel simples das ultimas leituras compensadas, para prever
// o comportamento da futura medicao real (media de ~20 amostras)
const int numAmostras = 20;
float amostras[numAmostras];
int indiceAmostra = 0;
int totalAmostras = 0;

void setup() {
  Serial.begin(115200);
  delay(500);

  if (!sensorTemp.begin()) {
    Serial.println("ERRO: sensor de temperatura nao encontrado no I2C.");
    Serial.println("Verificar ligacoes SDA=21, SCL=22 e alimentacao a 3.3V.");
    while (true) delay(1000);
  }

  Serial.println("Sensor de temperatura iniciado.");
  Serial.println("Colunas: objeto(cru) | objeto(compensado) | ambiente | media(20) | plausivel?");
}

void loop() {
  float objetoCru = sensorTemp.readObjectTempC();
  float ambiente = sensorTemp.readAmbientTempC();
  float compensado = objetoCru + offsetCalibracao;

  amostras[indiceAmostra] = compensado;
  indiceAmostra = (indiceAmostra + 1) % numAmostras;
  if (totalAmostras < numAmostras) totalAmostras++;

  float soma = 0;
  for (int i = 0; i < totalAmostras; i++) {
    soma += amostras[i];
  }
  float media = soma / totalAmostras;

  bool plausivel = media >= plausivelMinimo && media <= plausivelMaximo;

  Serial.print(objetoCru, 2);
  Serial.print(" C | ");
  Serial.print(compensado, 2);
  Serial.print(" C | ");
  Serial.print(ambiente, 2);
  Serial.print(" C | media: ");
  Serial.print(media, 2);
  Serial.print(" C | ");
  Serial.println(plausivel ? "PLAUSIVEL" : "INVALIDA");

  delay(200); // mesma cadencia de amostragem prevista para a medicao real
}

// Procedimento de calibracao:
// 1. Apontar o sensor a testa a 2-3 cm e esperar que a media estabilize
// 2. Medir a temperatura real com um termometro comercial
// 3. Ajustar offsetCalibracao pela diferenca e repetir ate as medias coincidirem
// 4. Validar a janela: apontar ao ar (deve dar INVALIDA), a testa (PLAUSIVEL)
//    e a um objeto quente, e.g. cha acabado de fazer (deve dar INVALIDA)
