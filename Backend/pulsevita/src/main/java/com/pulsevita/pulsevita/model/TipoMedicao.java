package com.pulsevita.pulsevita.model;

// O mesmo vocabulario circula de ponta a ponta: pedido REST da app,
// comando MQTT ao dispositivo e coluna tipo_medicao na base de dados
public enum TipoMedicao {
    TEMPERATURA,
    BPM,
    AMBOS
}
