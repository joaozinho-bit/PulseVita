# Comunicação MQTT - PulseVita

## Ativar comunicação entre Backend e ESP32 via MQTT

Para iniciar a comunicação MQTT, executar os comandos pela ordem indicada.

---

## 1. Ativar o broker MQTT

Abrir um terminal e navegar até à pasta do Mosquitto:

```bash
cd Hardware\Mosquitto
```

Iniciar o broker MQTT:

```bash
mosquitto -c mosquitto.conf -v
```

Este comando inicia o broker MQTT em modo verbose, permitindo visualizar as mensagens trocadas entre o Backend e o ESP32.

---

## 2. Iniciar o Backend

Abrir outro terminal e executar:

```bash
cd backend/pulsevita
./mvnw clean spring-boot:run
```

---

## Checklist de ligação

Antes de iniciar, garantir que:

- O computador e o ESP32 estão ligados à **mesma rede Wi-Fi**.
- O IP do broker MQTT está corretamente configurado nos ficheiros:

### Backend

```
Backend\pulsevita\src\main\resources\application.properties
```

### ESP32

```
Hardware\Mosquitto\mqtt\mqtt.ino
```

Confirmar que o IP configurado no ESP32 corresponde ao IP do computador onde o Mosquitto está a correr.