#include <Wire.h>
#include "MAX30105.h"
#include "heartRate.h"

MAX30105 particleSensor;

const byte RATE_SIZE = 10;
byte rates[RATE_SIZE];
byte rateSpot = 0;
byte ratesFilled = 0;
long lastBeat = 0;
long lastValidDelta = 0;

int beatAvg = 0;
bool stateDedoPresente = false;

const long IR_THRESHOLD = 50000;

void setup() {
  Serial.begin(115200);
  Wire.begin(16, 17);

  if (!particleSensor.begin(Wire, I2C_SPEED_STANDARD)) {
    Serial.println("MAX30102 nao encontrado.");
    while (1);
  }

  // 400 amostras/s com media de 4 suaviza o sinal sem perder resolucao temporal.
  particleSensor.setup(0x1F, 4, 2, 400, 411, 4096);
  particleSensor.setPulseAmplitudeGreen(0);

  Serial.println("Pousa o dedo e mantem-te parado.");
}

void resetMedicao() {
  rateSpot = 0;
  ratesFilled = 0;
  beatAvg = 0;
  lastBeat = 0;
  lastValidDelta = 0;
}

void loop() {
  long irValue = particleSensor.getIR();

  if (irValue < IR_THRESHOLD) {
    if (stateDedoPresente) {
      resetMedicao();
      stateDedoPresente = false;
    }
    Serial.println("Sem dedo");
    delay(200);
    return;
  }

  stateDedoPresente = true;

  if (checkForBeat(irValue)) {
    long now = millis();
    long delta = now - lastBeat;
    lastBeat = now;

    // 300-1500ms corresponde a 40-200 BPM. Fora disto e sempre artefacto.
    bool intervaloPlausivel = (delta > 300 && delta < 1500);

    // Rejeita variacoes bruscas face ao batimento anterior: o ritmo cardiaco
    // nao muda mais de ~30% entre batimentos consecutivos em repouso.
    bool variacaoAceitavel = true;
    if (lastValidDelta > 0) {
      float racio = (float)delta / lastValidDelta;
      variacaoAceitavel = (racio > 0.7 && racio < 1.4);
    }

    if (intervaloPlausivel && variacaoAceitavel) {
      lastValidDelta = delta;
      int bpm = 60000 / delta;

      rates[rateSpot++] = (byte)bpm;
      rateSpot %= RATE_SIZE;
      if (ratesFilled < RATE_SIZE) ratesFilled++;

      // So mostra media com amostras suficientes para ser fiavel.
      if (ratesFilled >= 5) {
        int sum = 0;
        for (byte i = 0; i < ratesFilled; i++) sum += rates[i];
        beatAvg = sum / ratesFilled;

        Serial.print("BPM: ");
        Serial.print(bpm);
        Serial.print("  Media: ");
        Serial.print(beatAvg);
        Serial.print("  (");
        Serial.print(ratesFilled);
        Serial.println(" amostras)");
      } else {
        Serial.print("A estabilizar... ");
        Serial.println(ratesFilled);
      }
    }
  }
}