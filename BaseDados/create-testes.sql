CREATE TABLE dispositivo (
    id INTEGER PRIMARY KEY,
    nome VARCHAR(100), -- "LinkVita"
    tipo VARCHAR(100) -- Mais para escalabilidade do projeto, para agora só vai existir um, por exemplo "Temp&Bpm Monitor"
);

CREATE TABLE utilizador (
    id INTEGER PRIMARY KEY,
    nome_completo VARCHAR(100),
    data_nascimento DATE,
    genero VARCHAR(1), -- M ou F
    username VARCHAR(100),
    email VARCHAR(100),
    password VARCHAR(100)
);

CREATE TABLE utilizador_dispositivo (
    id INTEGER PRIMARY KEY,
    id_utilizador INTEGER,
    id_dispositivo INTEGER,
    dispositivo_serial VARCHAR(100),
    data_associacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_utilizador) REFERENCES utilizador(id),
    FOREIGN KEY (id_dispositivo) REFERENCES dispositivo(id)
);

CREATE TABLE doenca (
    id INTEGER PRIMARY KEY,
    id_utilizador INTEGER,
    cronica BOOLEAN,
    nome VARCHAR(100),
    observacoes VARCHAR(255),
    FOREIGN KEY (id_utilizador) REFERENCES utilizador(id)
);

CREATE TABLE medico (
    id INTEGER PRIMARY KEY,
    id_cartao INTEGER,
    nome VARCHAR(100),
    especializacao VARCHAR(100)
);

CREATE TABLE estado_consulta (
    estado VARCHAR(50) PRIMARY KEY,
    descricao VARCHAR(255)
);

INSERT INTO estado_consulta (estado, descricao) VALUES
    ('POR_CONFIRMAR', 'Consulta por confirmar'),
    ('CONFIRMADA', 'Consulta confirmada'),
    ('CANCELADA', 'Consulta cancelada'),
    ('CONCLUIDA', 'Consulta concluída');

CREATE TABLE consulta (
    id INTEGER PRIMARY KEY,
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
    id INTEGER PRIMARY KEY,
    id_utilizador INTEGER,
    freq_cardiaca INTEGER,
    temperatura DECIMAL(4,2),
    data_leitura TIMESTAMP,
    FOREIGN KEY (id_utilizador) REFERENCES utilizador(id)
);