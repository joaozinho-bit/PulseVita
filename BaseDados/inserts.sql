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
    (92674206, '967545', '123', 'Dr. Carlos Pinto', 'Cardiologia');


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


-- =====================================================================
--  DADOS DE DEMONSTRAÇÃO (acrescentados)
--  Sistema com histórico de utilização real: vários pacientes, médicos,
--  consultas, doenças e medições coerentes entre si.
--  Paciente principal da apresentação: Matilde Vasconcelos (paciente id 3).
--
--  NOTA IMPORTANTE — coluna consulta.motivo:
--  O código (Consulta.java / ConsultaController) usa a coluna "motivo", mas
--  ela NÃO existe no create.sql. Em runtime é criada pelo Hibernate
--  (spring.jpa.hibernate.ddl-auto=update). Para que estes INSERTs também
--  funcionem contra uma base criada apenas a partir do create.sql, garante-se
--  a coluna aqui de forma idempotente (não altera o create.sql).
--  Correção recomendada: acrescentar "motivo VARCHAR(255)" à tabela consulta
--  no create.sql.
-- =====================================================================
ALTER TABLE consulta ADD COLUMN IF NOT EXISTS motivo VARCHAR(255);


-- ---------------------------------------------------------------------
--  Dispositivos adicionais (ids 3..7)
-- ---------------------------------------------------------------------
INSERT INTO dispositivos (mac_address, id_dispositivo) VALUES
    ('AA:BB:CC:DD:EE:03', 'PV-Q4R7N2'),   -- id 3 -> Matilde (demo)
    ('AA:BB:CC:DD:EE:04', 'PV-H8B3W5'),   -- id 4 -> Rafael
    ('AA:BB:CC:DD:EE:05', 'PV-K2D9F6'),   -- id 5 -> Beatriz
    ('AA:BB:CC:DD:EE:06', 'PV-T5G1J8'),   -- id 6 -> Gonçalo
    ('AA:BB:CC:DD:EE:07', 'PV-C3P7L4');   -- id 7 -> Tomás


-- ---------------------------------------------------------------------
--  Médicos adicionais (ids 2..5). Especialidades coerentes.
-- ---------------------------------------------------------------------
INSERT INTO medico
(id_cartao, n_medico, password, nome_completo, especializacao)
VALUES
    ('48317209', '551302', '123', 'Dra. Marta Bragança',    'Medicina Geral e Familiar'),  -- id 2
    ('73920541', '618477', '123', 'Dr. Nuno Valadares',     'Cardiologia'),                -- id 3
    ('15084376', '742911', '123', 'Dra. Cláudia Meireles',  'Medicina Interna'),           -- id 4
    ('60275198', '803654', '123', 'Dr. Henrique Bettencourt','Pneumologia');               -- id 5


-- ---------------------------------------------------------------------
--  Doenças adicionais (ids 7..10) — coerentes com monitorização de
--  frequência cardíaca e temperatura.
-- ---------------------------------------------------------------------
INSERT INTO doenca (nome, cronica, observacoes) VALUES
    ('Hipotiroidismo', TRUE,
     'Função tiroideia diminuída; pode provocar bradicardia e cansaço.'),          -- id 7
    ('Hipertiroidismo', TRUE,
     'Função tiroideia aumentada; pode provocar taquicardia e febrícula.'),        -- id 8
    ('Anemia', FALSE,
     'Défice de glóbulos vermelhos; pode causar taquicardia compensatória.'),      -- id 9
    ('Diabetes Mellitus Tipo 2', TRUE,
     'Doença metabólica crónica com necessidade de acompanhamento regular.');      -- id 10


-- ---------------------------------------------------------------------
--  Pacientes adicionais (ids 3..8)
--  data_registo explícita para simular utilização já com algum tempo.
--  id 3 Matilde -> paciente principal (o mais completo).
--  id 7 Inês    -> ainda sem dispositivo associado (estado de onboarding).
-- ---------------------------------------------------------------------
INSERT INTO paciente
(n_paciente, nome_completo, data_nascimento, genero, email, password, telefone,
 id_dispositivo, data_registo, data_associacao)
VALUES
    -- id 3: PACIENTE PRINCIPAL DA DEMONSTRAÇÃO
    ('204518637', 'Matilde Vasconcelos', '1968-06-14', 'F',
     'matilde.vasconcelos@email.com', 'password123', '916402738',
     3, '2024-09-05 09:15:00', '2024-09-05 09:40:00'),

    -- id 4: Rafael, atleta (frequência cardíaca de repouso baixa)
    ('315926480', 'Rafael Quintela', '1994-02-19', 'M',
     'rafael.quintela@email.com', 'password123', '934517620',
     4, '2025-01-03 08:00:00', '2025-01-05 10:05:00'),

    -- id 5: Beatriz, jovem saudável, poucos dados
    ('268401957', 'Beatriz Sampaio', '2001-08-27', 'F',
     'beatriz.sampaio@email.com', 'password123', '962088415',
     5, '2026-05-14 11:30:00', '2026-05-15 08:50:00'),

    -- id 6: Gonçalo, idoso com hipertensão e diabetes
    ('197354028', 'Gonçalo Madureira', '1955-11-03', 'M',
     'goncalo.madureira@email.com', 'password123', '913774509',
     6, '2025-07-15 10:00:00', '2025-08-01 16:20:00'),

    -- id 7: Inês, registada mas ainda SEM dispositivo (sem medições)
    ('350147982', 'Inês Rebelo', '1988-05-16', 'F',
     'ines.rebelo@email.com', 'password123', '927650183',
     NULL, '2026-06-10 14:45:00', NULL),

    -- id 8: Tomás, criança (11 anos) — exercita os limites de BPM por idade
    ('289610345', 'Tomás Falcão', '2015-03-08', 'M',
     'tomas.falcao@email.com', 'password123', '935120467',
     7, '2025-10-15 09:30:00', '2025-10-20 17:10:00');


-- ---------------------------------------------------------------------
--  Valores de referência personalizados do paciente principal (Matilde)
--  Cenário clínico: Fibrilhação Auricular com estratégia de controlo de
--  frequência permissiva (alvo de repouso até ~115 bpm). O cardiologista
--  (médico id 1) define limites próprios, definidos em 2025-02-27, uma
--  semana após o diagnóstico. ANTES desta data a app usou os limites padrão
--  (60-100); DEPOIS passa a usar 55-115 automaticamente — é isto que se
--  demonstra: valores de ~104-112 bpm passam de "BPM elevado" a "Normal".
-- ---------------------------------------------------------------------
INSERT INTO valores_referencia_paciente
(id_paciente, id_medico, bpm_minimo, bpm_maximo, temperatura_maxima, data_definicao)
VALUES
    (3, 1, 55, 115, 37.5, '2025-02-27 12:00:00');


-- ---------------------------------------------------------------------
--  Associação pacientes <-> doenças
--  data_fim NULL = doença ativa; preenchida = doença terminada.
-- ---------------------------------------------------------------------
INSERT INTO paciente_doenca
(id_paciente, id_doenca, data_diagnostico, data_fim, id_medico)
VALUES
    -- Matilde (id 3): condições cardíacas crónicas + uma gripe já resolvida
    (3, 4, '2024-10-15', NULL,         2),   -- Hipertensão Arterial (ativa), Dra. Marta Bragança
    (3, 3, '2025-02-20', NULL,         1),   -- Fibrilhação Auricular (ativa), Dr. Carlos Pinto
    (3, 6, '2026-01-12', '2026-01-22', 2),   -- Gripe (resolvida)

    -- Rafael (id 4): gripe pontual já resolvida
    (4, 6, '2025-01-10', '2025-01-20', 2),

    -- Gonçalo (id 6): hipertensão e diabetes ativas
    (6, 4, '2025-08-20', NULL, 1),           -- Hipertensão Arterial
    (6, 10, '2025-10-05', NULL, 4),          -- Diabetes Mellitus Tipo 2, Dra. Cláudia Meireles

    -- Inês (id 7): hipotiroidismo ativo (sem dispositivo, mas com registo clínico)
    (7, 7, '2026-06-15', NULL, 4),

    -- Tomás (id 8): pneumonia já resolvida
    (8, 5, '2025-11-04', '2025-11-25', 5);   -- Dr. Henrique Bettencourt


-- ---------------------------------------------------------------------
--  Consultas (estados: POR_CONFIRMAR, CONFIRMADA, CANCELADA, CONCLUIDA)
--  Distribuídas ao longo de vários meses. Consultas pendentes concentradas
--  no mês corrente (julho/2026) para poderem ser confirmadas/recusadas ao
--  vivo no calendário do médico. id_medico NULL nas pendentes (o médico é
--  atribuído no momento da confirmação).
-- ---------------------------------------------------------------------
INSERT INTO consulta
(id_medico, id_paciente, estado, data_consulta, hora_consulta, data_criacao, motivo)
VALUES
    -- Matilde (id 3): historial completo de acompanhamento cardiológico
    (2, 3, 'CONCLUIDA',     '2024-11-08', '09:30:00', '2024-10-28 14:12:00', 'Avaliação de tensão arterial elevada'),
    (1, 3, 'CONCLUIDA',     '2025-02-20', '11:00:00', '2025-02-10 10:05:00', 'Consulta de cardiologia por palpitações'),
    (1, 3, 'CONCLUIDA',     '2025-06-16', '10:30:00', '2025-06-02 16:40:00', 'Seguimento de fibrilhação auricular'),
    (1, 3, 'CONCLUIDA',     '2025-11-24', '09:00:00', '2025-11-10 09:20:00', 'Revisão de medicação e controlo de tensão arterial'),
    (1, 3, 'CONCLUIDA',     '2026-04-13', '16:00:00', '2026-03-30 11:00:00', 'Consulta de rotina de cardiologia'),
    (1, 3, 'CANCELADA',     '2026-06-05', '15:00:00', '2026-05-22 13:30:00', 'Consulta de rotina (reagendada)'),
    (NULL, 3, 'POR_CONFIRMAR','2026-07-31', '10:00:00', '2026-07-21 19:15:00', 'Seguimento de fibrilhação auricular'),
    (1, 3, 'CONFIRMADA',    '2026-08-12', '10:00:00', '2026-07-15 10:45:00', 'Consulta de controlo de hipertensão'),

    -- Ana Martins (id 1): utente existente — uma consulta futura confirmada
    (3, 1, 'CONFIRMADA',    '2026-08-19', '10:00:00', '2026-07-24 09:00:00', 'Consulta de rotina'),

    -- João Ferreira (id 2): utente existente com bradicardia — consulta futura
    (1, 2, 'CONFIRMADA',    '2026-09-08', '09:30:00', '2026-07-24 09:05:00', 'Seguimento de bradicardia sinusal'),

    -- Rafael (id 4)
    (2, 4, 'CONCLUIDA',     '2025-01-15', '09:30:00', '2025-01-08 08:50:00', 'Consulta por sintomas gripais'),
    (NULL, 4, 'POR_CONFIRMAR','2026-07-28', '11:00:00', '2026-07-19 20:10:00', 'Renovação de atestado médico-desportivo'),

    -- Beatriz (id 5)
    (NULL, 5, 'POR_CONFIRMAR','2026-07-30', '16:30:00', '2026-07-22 12:00:00', 'Primeira consulta de rotina'),

    -- Gonçalo (id 6)
    (4, 6, 'CONCLUIDA',     '2026-02-18', '10:00:00', '2026-02-04 15:30:00', 'Controlo de hipertensão e diabetes'),
    (4, 6, 'CONFIRMADA',    '2026-08-28', '15:00:00', '2026-07-18 11:20:00', 'Seguimento de diabetes'),

    -- Inês (id 7)
    (NULL, 7, 'POR_CONFIRMAR','2026-07-29', '14:00:00', '2026-07-16 17:45:00', 'Avaliação da função da tiroide'),

    -- Tomás (id 8)
    (5, 8, 'CONCLUIDA',     '2025-11-05', '09:00:00', '2025-11-04 20:30:00', 'Consulta por febre e tosse persistente'),
    (5, 8, 'CONFIRMADA',    '2026-08-06', '10:30:00', '2026-07-17 09:40:00', 'Consulta de seguimento pós-pneumonia');


-- ---------------------------------------------------------------------
--  Histórico de medições — Matilde (paciente id 3), o mais completo.
--  tipo_medicao: TEMPERATURA / BPM / AMBOS.
--  As avaliações refletem os limites EM VIGOR à data da leitura:
--    * até 2025-02-27  -> limites padrão (60-100 bpm, 37.5 ºC)
--    * a partir daí     -> limites personalizados (55-115 bpm, 37.5 ºC)
--  Repare-se em 2024-10-12/11-15 (105-101 bpm = "BPM_ALTO" com o padrão) vs
--  2025-03-06 em diante (104-113 bpm = "NORMAL" com os limites do médico).
-- ---------------------------------------------------------------------
INSERT INTO historico_paciente
(id_paciente, tipo_medicao, temperatura, bpm, avaliacao, data_leitura)
VALUES
    -- FASE 1: antes dos limites personalizados (limites padrão 60-100)
    (3, 'AMBOS',       36.6,  82, 'NORMAL',    '2024-09-14 08:20:00'),
    (3, 'BPM',         NULL,  96, 'NORMAL',    '2024-09-28 21:10:00'),
    (3, 'AMBOS',       36.8, 101, 'BPM_ALTO',  '2024-10-12 09:05:00'),
    (3, 'TEMPERATURA', 36.9, NULL,'NORMAL',    '2024-10-30 19:45:00'),
    (3, 'AMBOS',       36.7, 105, 'BPM_ALTO',  '2024-11-15 07:50:00'),
    (3, 'BPM',         NULL,  88, 'NORMAL',    '2024-12-08 22:30:00'),
    (3, 'BPM',         NULL, 107, 'BPM_ALTO',  '2025-01-13 08:15:00'),
    (3, 'AMBOS',       36.5, 109, 'BPM_ALTO',  '2025-02-05 20:00:00'),
    (3, 'BPM',         NULL, 114, 'BPM_ALTO',  '2025-02-18 10:40:00'),

    -- FASE 2: depois dos limites personalizados (55-115) — mesmos valores, agora NORMAL
    (3, 'AMBOS',       36.7, 106, 'NORMAL',    '2025-03-06 08:30:00'),
    (3, 'BPM',         NULL,  98, 'NORMAL',    '2025-03-22 21:15:00'),
    (3, 'AMBOS',       36.6, 110, 'NORMAL',    '2025-04-10 07:45:00'),
    (3, 'TEMPERATURA', 36.8, NULL,'NORMAL',    '2025-05-02 18:20:00'),
    (3, 'BPM',         NULL, 112, 'NORMAL',    '2025-05-19 09:10:00'),
    (3, 'AMBOS',       36.9, 108, 'NORMAL',    '2025-06-15 08:00:00'),
    (3, 'BPM',         NULL, 121, 'BPM_ALTO',  '2025-07-07 22:05:00'),  -- episódio de taquicardia real (>115)
    (3, 'AMBOS',       36.5, 104, 'NORMAL',    '2025-08-12 08:25:00'),
    (3, 'BPM',         NULL,  60, 'NORMAL',    '2025-09-01 20:40:00'),
    (3, 'AMBOS',       36.7, 111, 'NORMAL',    '2025-10-14 07:35:00'),
    (3, 'BPM',         NULL, 118, 'BPM_ALTO',  '2025-11-20 09:00:00'),
    (3, 'AMBOS',       36.8, 102, 'NORMAL',    '2025-12-09 21:30:00'),
    -- Episódio de gripe (diagnóstico 2026-01-12): febre + frequência elevada
    (3, 'AMBOS',       38.3, 119, 'FEBRE,BPM_ALTO', '2026-01-13 08:10:00'),
    (3, 'TEMPERATURA', 38.0, NULL,'FEBRE',     '2026-01-16 09:20:00'),
    (3, 'AMBOS',       37.2, 110, 'NORMAL',    '2026-01-19 20:00:00'),  -- recuperação
    (3, 'BPM',         NULL, 107, 'NORMAL',    '2026-02-15 08:40:00'),
    (3, 'AMBOS',       36.6, 113, 'NORMAL',    '2026-03-11 07:50:00'),
    (3, 'BPM',         NULL,  99, 'NORMAL',    '2026-04-12 22:15:00'),
    (3, 'AMBOS',       36.7, 116, 'BPM_ALTO',  '2026-05-08 08:05:00'),  -- ligeiramente acima de 115
    (3, 'TEMPERATURA', 36.9, NULL,'NORMAL',    '2026-06-20 19:50:00'),
    (3, 'AMBOS',       36.8, 109, 'NORMAL',    '2026-07-05 08:30:00'),
    (3, 'BPM',         NULL, 112, 'NORMAL',    '2026-07-18 21:00:00'),
    (3, 'AMBOS',       36.6, 105, 'NORMAL',    '2026-07-23 08:15:00');  -- leitura mais recente


-- ---------------------------------------------------------------------
--  Histórico — Rafael (id 4): atleta, sem limites personalizados.
--  Com os limites padrão (60-100), a sua frequência de repouso baixa
--  gera alguns "BPM_BAIXO" (contraste com a Matilde, que tem limites do médico).
-- ---------------------------------------------------------------------
INSERT INTO historico_paciente
(id_paciente, tipo_medicao, temperatura, bpm, avaliacao, data_leitura)
VALUES
    (4, 'BPM',         NULL,  58, 'BPM_BAIXO', '2025-02-02 07:30:00'),
    (4, 'AMBOS',       36.5,  62, 'NORMAL',    '2025-04-18 08:00:00'),
    (4, 'TEMPERATURA', 36.7, NULL,'NORMAL',    '2025-07-25 21:00:00'),
    (4, 'BPM',         NULL,  66, 'NORMAL',    '2025-10-10 07:15:00'),
    (4, 'AMBOS',       36.6,  59, 'BPM_BAIXO', '2026-02-14 08:20:00'),
    (4, 'BPM',         NULL,  64, 'NORMAL',    '2026-06-30 09:00:00');


-- ---------------------------------------------------------------------
--  Histórico — Beatriz (id 5): jovem saudável, poucos dados, tudo normal.
-- ---------------------------------------------------------------------
INSERT INTO historico_paciente
(id_paciente, tipo_medicao, temperatura, bpm, avaliacao, data_leitura)
VALUES
    (5, 'AMBOS',       36.6,  74, 'NORMAL', '2026-05-20 09:10:00'),
    (5, 'BPM',         NULL,  81, 'NORMAL', '2026-06-12 20:30:00'),
    (5, 'TEMPERATURA', 36.8, NULL,'NORMAL', '2026-07-10 08:45:00');


-- ---------------------------------------------------------------------
--  Histórico — Gonçalo (id 6): idoso, limites padrão. Uma febre invernal
--  e um episódio de BPM elevado.
-- ---------------------------------------------------------------------
INSERT INTO historico_paciente
(id_paciente, tipo_medicao, temperatura, bpm, avaliacao, data_leitura)
VALUES
    (6, 'AMBOS',       36.7,  78, 'NORMAL',   '2025-08-15 08:30:00'),
    (6, 'BPM',         NULL,  92, 'NORMAL',   '2025-09-20 19:00:00'),
    (6, 'AMBOS',       37.0,  88, 'NORMAL',   '2025-11-11 08:00:00'),
    (6, 'TEMPERATURA', 37.8, NULL,'FEBRE',    '2026-01-25 07:45:00'),
    (6, 'BPM',         NULL, 103, 'BPM_ALTO', '2026-03-30 21:15:00'),
    (6, 'AMBOS',       36.8,  85, 'NORMAL',   '2026-07-02 08:10:00');


-- ---------------------------------------------------------------------
--  Histórico — Tomás (id 8): criança (11 anos). Os limites por IDADE
--  (75-118 bpm) tornam normais frequências que num adulto seriam elevadas.
--  Pneumonia em novembro/2025: febre + frequência muito alta.
-- ---------------------------------------------------------------------
INSERT INTO historico_paciente
(id_paciente, tipo_medicao, temperatura, bpm, avaliacao, data_leitura)
VALUES
    (8, 'AMBOS',       36.7,  98, 'NORMAL',        '2025-10-28 18:30:00'),
    (8, 'AMBOS',       38.6, 124, 'FEBRE,BPM_ALTO','2025-11-04 08:00:00'),  -- início da pneumonia
    (8, 'TEMPERATURA', 38.1, NULL,'FEBRE',         '2025-11-08 20:00:00'),
    (8, 'AMBOS',       37.1, 108, 'NORMAL',        '2025-11-15 09:30:00'),  -- recuperação (108<=118 p/ criança)
    (8, 'BPM',         NULL,  96, 'NORMAL',        '2026-01-20 08:15:00'),
    (8, 'AMBOS',       36.6,  90, 'NORMAL',        '2026-05-12 17:45:00');