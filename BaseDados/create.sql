-- Script para criar as tabelas necessárias

-- Limpa as tabelas existentes para evitar conflitos
DROP TABLE IF EXISTS historico_paciente;
DROP TABLE IF EXISTS consulta;
DROP TABLE IF EXISTS estado_consulta;
DROP TABLE IF EXISTS valores_referencia_paciente;
DROP TABLE IF EXISTS paciente_doenca;
DROP TABLE IF EXISTS medico;
DROP TABLE IF EXISTS doenca;
DROP TABLE IF EXISTS paciente;
DROP TABLE IF EXISTS dispositivos;

-- Dispositivos fabricados pela empresa
CREATE TABLE dispositivos (
    id SERIAL PRIMARY KEY,
    mac_address VARCHAR(17) UNIQUE NOT NULL,
    id_dispositivo VARCHAR(20) UNIQUE NOT NULL -- Código usado para associar o dispositivo na app
);

CREATE TABLE paciente (
    id SERIAL PRIMARY KEY,
    n_paciente VARCHAR(20) UNIQUE NOT NULL,
    nome_completo VARCHAR(100),
    data_nascimento DATE,
    genero VARCHAR(1),
    email VARCHAR(100),
    password VARCHAR(100),
    telefone VARCHAR(9),
    foto_perfil VARCHAR(255),
    data_registo TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_dispositivo INTEGER UNIQUE,
    data_associacao TIMESTAMP,
    FOREIGN KEY (id_dispositivo) REFERENCES dispositivos(id)
);

-- Doenças
CREATE TABLE doenca (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) UNIQUE NOT NULL,
    cronica BOOLEAN,
    observacoes VARCHAR(255)
);

CREATE TABLE medico (
    id SERIAL PRIMARY KEY,
    id_cartao INTEGER UNIQUE,
    n_medico VARCHAR(20) UNIQUE,
    password VARCHAR(100),
    nome_completo VARCHAR(100),
    especializacao VARCHAR(100)
);

-- Relação M:M entre pacientes e doenças
CREATE TABLE paciente_doenca (
    id_paciente INTEGER NOT NULL,
    id_doenca INTEGER NOT NULL,
    data_diagnostico DATE,
    data_fim DATE,
    id_medico INTEGER NOT NULL,
    PRIMARY KEY (id_paciente, id_doenca),
    FOREIGN KEY (id_paciente) REFERENCES paciente(id),
    FOREIGN KEY (id_doenca) REFERENCES doenca(id),
    FOREIGN KEY (id_medico) REFERENCES medico(id)
);

-- Valores personalizados definidos pelo médico para um paciente.
-- Caso não exista registo, o backend utiliza os valores padrão.
CREATE TABLE valores_referencia_paciente (
    id SERIAL PRIMARY KEY,
    id_paciente INTEGER NOT NULL,
    id_medico INTEGER NOT NULL,
    bpm_minimo INTEGER,
    bpm_maximo INTEGER,
    temperatura_maxima DECIMAL(4,2),
    data_definicao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_paciente) REFERENCES paciente(id),
    FOREIGN KEY (id_medico) REFERENCES medico(id)
);

CREATE TABLE estado_consulta (
    estado VARCHAR(50) PRIMARY KEY,
    descricao VARCHAR(255)
);

CREATE TABLE consulta (
    id SERIAL PRIMARY KEY,
    id_medico INTEGER,
    id_paciente INTEGER NOT NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'POR_CONFIRMAR',
    data_consulta DATE NOT NULL,
    hora_consulta TIME NOT NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_medico) REFERENCES medico(id),
    FOREIGN KEY (id_paciente) REFERENCES paciente(id),
    FOREIGN KEY (estado) REFERENCES estado_consulta(estado)
);

CREATE TABLE historico_paciente (
    id SERIAL PRIMARY KEY,
    id_paciente INTEGER NOT NULL,
    bpm INTEGER NOT NULL,
    temperatura DECIMAL(4,2) NOT NULL,
    data_leitura TIMESTAMP NOT NULL,
    FOREIGN KEY (id_paciente) REFERENCES paciente(id)
);