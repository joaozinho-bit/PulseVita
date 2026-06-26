-- Script para criar as tabelas necessárias

-- Limpa as tabelas existentes para evitar conflitos
DROP TABLE IF EXISTS limite_paciente;
DROP TABLE IF EXISTS historico_utilizador;
DROP TABLE IF EXISTS consulta;
DROP TABLE IF EXISTS estado_consulta;
DROP TABLE IF EXISTS doenca;
DROP TABLE IF EXISTS utilizador_dispositivo;
DROP TABLE IF EXISTS medico;
DROP TABLE IF EXISTS utilizador;
DROP TABLE IF EXISTS dispositivo;

-- Catálogo dos modelos de dispositivo vendidos pela empresa (ex: LinkLife, futuros modelos)
CREATE TABLE dispositivo (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100),
    tipo VARCHAR(100)
);

CREATE TABLE utilizador (
    id SERIAL PRIMARY KEY,
    n_utente VARCHAR(20) UNIQUE,
    nome_completo VARCHAR(100),
    data_nascimento DATE,
    genero VARCHAR(1), -- M ou F
    username VARCHAR(100),
    email VARCHAR(100),
    password VARCHAR(100),
    telefone VARCHAR(9),
    foto_perfil VARCHAR(255),
    data_registo TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Associa uma unidade física de um dispositivo a um utilizador.
-- mac_address é interno (nunca mostrado ao utilizador).
-- id_dispositivo_curto é o código impresso no produto que o utilizador introduz na app.
CREATE TABLE utilizador_dispositivo (
    id SERIAL PRIMARY KEY,
    id_utilizador INTEGER,
    id_dispositivo INTEGER,
    mac_address VARCHAR(17) UNIQUE,
    id_dispositivo_curto VARCHAR(20) UNIQUE,
    data_associacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_utilizador) REFERENCES utilizador(id),
    FOREIGN KEY (id_dispositivo) REFERENCES dispositivo(id)
);

CREATE TABLE doenca (
    id SERIAL PRIMARY KEY,
    id_utilizador INTEGER,
    cronica BOOLEAN,
    nome VARCHAR(100),
    observacoes VARCHAR(255),
    FOREIGN KEY (id_utilizador) REFERENCES utilizador(id)
);

CREATE TABLE medico (
    id SERIAL PRIMARY KEY,
    id_cartao INTEGER,
    nome VARCHAR(100),
    especializacao VARCHAR(100)
);

-- Limites de alerta personalizados, definidos pelo médico para cada paciente.
-- Se não existir registo para o utilizador, o backend aplica os valores padrão (ex: bpm 60-100, temp 37.5).
CREATE TABLE limite_paciente (
    id SERIAL PRIMARY KEY,
    id_utilizador INTEGER,
    id_medico INTEGER,
    bpm_minimo INTEGER,
    bpm_maximo INTEGER,
    temperatura_maxima DECIMAL(4,2),
    data_definicao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_utilizador) REFERENCES utilizador(id),
    FOREIGN KEY (id_medico) REFERENCES medico(id)
);

CREATE TABLE estado_consulta (
    estado VARCHAR(50) PRIMARY KEY,
    descricao VARCHAR(255)
);

CREATE TABLE consulta (
    id SERIAL PRIMARY KEY,
    id_medico INTEGER,
    id_utilizador INTEGER,
    estado VARCHAR(50) DEFAULT 'POR_CONFIRMAR',
    data_consulta DATE,
    hora_consulta TIME,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_medico) REFERENCES medico(id),
    FOREIGN KEY (id_utilizador) REFERENCES utilizador(id),
    FOREIGN KEY (estado) REFERENCES estado_consulta(estado)
);

CREATE TABLE historico_utilizador (
    id SERIAL PRIMARY KEY,
    id_utilizador INTEGER,
    freq_cardiaca INTEGER,
    temperatura DECIMAL(4,2),
    data_leitura TIMESTAMP,
    FOREIGN KEY (id_utilizador) REFERENCES utilizador(id)
);