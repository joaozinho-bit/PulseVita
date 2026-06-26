-- Script para inserir dados iniciais nas tabelas

-- Estados possíveis de uma consulta
INSERT INTO estado_consulta (estado, descricao) VALUES
    ('POR_CONFIRMAR', 'Consulta por confirmar'),
    ('CONFIRMADA', 'Consulta confirmada'),
    ('CANCELADA', 'Consulta cancelada'),
    ('CONCLUIDA', 'Consulta concluída');

-- Dispositivo de leitura de temperatura e frequência cardíaca
INSERT INTO dispositivo (nome, tipo) VALUES
    ('LinkLife', 'Medidor de temperatura e frequência cardíaca');

-- Utilizadores
-- Utilizador 1: saudável, sem condições que alterem os valores normais
INSERT INTO utilizador (n_utente, nome_completo, data_nascimento, genero, username, email, password, telefone, data_registo) VALUES
    ('123456789', 'Ana Martins', '1990-04-12', 'F', 'ana.martins', 'ana.martins@email.com', 'password123', '912345678', CURRENT_TIMESTAMP);

-- Utilizador 2: tem uma condição cardíaca que altera o que é considerado um BPM normal para ele
INSERT INTO utilizador (n_utente, nome_completo, data_nascimento, genero, username, email, password, telefone, data_registo) VALUES
    ('987654321', 'João Ferreira', '1965-09-30', 'M', 'joao.ferreira', 'joao.ferreira@email.com', 'password123', '923456789', CURRENT_TIMESTAMP);

-- Doença do utilizador 2 que justifica o ajuste dos limites de alerta
INSERT INTO doenca (id_utilizador, cronica, nome, observacoes) VALUES
    (2, TRUE, 'Bradicardia sinusal', 'Frequência cardíaca de repouso naturalmente mais baixa que a média, acompanhada por cardiologia.');

-- Médico
INSERT INTO medico (id_cartao, numero_medico, password, nome, especializacao) VALUES
    (100234, '967545', '123', 'Dr. Carlos Pinto', 'Cardiologia');

-- Associação do dispositivo ao utilizador 1 (mac_address fictício, substituir pelo real)
INSERT INTO utilizador_dispositivo (id_utilizador, id_dispositivo, mac_address, id_dispositivo_curto, data_associacao) VALUES
    (1, 1, 'AA:BB:CC:DD:EE:01', 'PV-X7K2M9', CURRENT_TIMESTAMP);

-- Associação do dispositivo ao utilizador 2
INSERT INTO utilizador_dispositivo (id_utilizador, id_dispositivo, mac_address, id_dispositivo_curto, data_associacao) VALUES
    (2, 1, 'AA:BB:CC:DD:EE:02', 'PV-M3T8L1', CURRENT_TIMESTAMP);

-- Limites personalizados para o utilizador 2, definidos pelo médico devido à bradicardia sinusal
INSERT INTO limite_paciente (id_utilizador, id_medico, bpm_minimo, bpm_maximo, temperatura_maxima, data_definicao) VALUES
    (2, 1, 45, 90, 37.5, CURRENT_TIMESTAMP);