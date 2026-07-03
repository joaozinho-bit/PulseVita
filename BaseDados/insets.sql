-- Script para inserir dados iniciais nas tabelas

-- Estados possíveis de uma consulta
INSERT INTO estado_consulta (estado, descricao) VALUES
    ('POR_CONFIRMAR', 'Consulta por confirmar'),
    ('CONFIRMADA', 'Consulta confirmada'),
    ('CANCELADA', 'Consulta cancelada'),
    ('CONCLUIDA', 'Consulta concluída');


-- Dispositivos fabricados pela empresa
INSERT INTO dispositivos (mac_address, id_dispositivo) VALUES
    ('AA:BB:CC:DD:EE:01', 'PV-X7K2M9'),
    ('AA:BB:CC:DD:EE:02', 'PV-M3T8L1');


-- Pacientes
INSERT INTO paciente
(n_paciente, nome_completo, data_nascimento, genero, email, password, telefone, id_dispositivo, data_associacao)
VALUES
    ('123456789', 'Ana Martins', '1990-04-12', 'F',
     'ana.martins@email.com', 'password123', '912345678', 1, CURRENT_TIMESTAMP),

    ('987654321', 'João Ferreira', '1965-09-30', 'M',
     'joao.ferreira@email.com', 'password123', '923456789', 2, CURRENT_TIMESTAMP);


-- Médico
INSERT INTO medico
(id_cartao, n_medico, password, nome_completo, especializacao)
VALUES
    (100234, '967545', '123', 'Dr. Carlos Pinto', 'Cardiologia');


-- Catálogo de doenças
INSERT INTO doenca (nome, cronica, observacoes) VALUES
    ('Bradicardia Sinusal', TRUE,
     'Frequência cardíaca inferior ao normal em repouso.'),

    ('Taquicardia', TRUE,
     'Frequência cardíaca persistentemente elevada.'),

    ('Fibrilhação Auricular', TRUE,
     'Arritmia cardíaca que provoca ritmo irregular.'),

    ('Hipertensão Arterial', TRUE,
     'Pressão arterial elevada.'),

    ('Pneumonia', FALSE,
     'Pode provocar febre e aumento da frequência cardíaca.'),

    ('Gripe', FALSE,
     'Infeção viral frequentemente acompanhada de febre.');


-- Associação entre pacientes e doenças
INSERT INTO paciente_doenca
(id_paciente, id_doenca, data_diagnostico, data_fim, id_medico)
VALUES
    (2, 1, '2023-05-12', NULL, 1);


-- Valores de referência personalizados
INSERT INTO valores_referencia_paciente
(id_paciente, id_medico, bpm_minimo, bpm_maximo, temperatura_maxima)
VALUES
    (2, 1, 45, 90, 37.5);