-- =====================================================================
-- Projeto de Banco de Dados — Carga de dados de exemplo
-- Executar DEPOIS de schema.sql e objetos.sql.
--
-- Os IDs são gerados pelo banco (IDENTITY), por isso o script começa
-- zerando as tabelas: assim os IDs sempre começam em 1 e as FKs abaixo
-- batem. Pode ser executado várias vezes.
--
-- Todos os usuários têm a senha "senha123".
-- Os domínios terminados em .test são fictícios (o TLD .test é reservado
-- para testes e não existe na internet).
-- =====================================================================

TRUNCATE perfis, usuarios, rede_local, servidor_dns, dispositivo, cat_dominio,
         dominio, lista_bloqueio, regra_personalizada, requisicoes,
         alertas_seguranca, usam, contem, tem
RESTART IDENTITY CASCADE;


-- Perfis (ids 1–10) ----------------------------------------------------
INSERT INTO perfis (nomeperfil, descricao) VALUES
('Administrador',   'Acesso total ao sistema'),
('Responsável',     'Gerencia a rede da casa e as regras de bloqueio'),
('Membro',          'Usuário comum da casa'),
('Convidado',       'Acesso temporário de visitantes'),
('Criança',         'Perfil com filtros mais rígidos'),
('Adolescente',     'Perfil com filtros moderados'),
('Técnico',         'Suporte técnico da rede'),
('Auditor',         'Apenas consulta o histórico e os alertas'),
('Home office',     'Usuário que trabalha de casa'),
('Somente leitura', 'Visualiza sem alterar');


-- Usuários (ids 1–10) --------------------------------------------------
INSERT INTO usuarios (idperfis, nome, email, senha_hash) VALUES
(1, 'Ana Souza',       'ana.souza@exemplo.com',       'pbkdf2$120000$sRvHWoBpxvcfmOjNNE81NQ==$D8fGrKE6tQdvt/mNLVbCE6EwAXQhegdRux4NpU4NyuQ='),
(2, 'Bruno Lima',      'bruno.lima@exemplo.com',      'pbkdf2$120000$y3PdVrna4/WRoJJCLbNdIw==$UPkP6vMZGuA+FP86ElRyRXRN2s8YrF39Jqw/SX6VCr0='),
(9, 'Carla Mendes',    'carla.mendes@exemplo.com',    'pbkdf2$120000$3J23QB0Nvk36S0rFSRfcNw==$ittLC41Ps6K0QCQiA509WfNvdR9cOXyGuNtmT8ExDII='),
(6, 'Diego Rocha',     'diego.rocha@exemplo.com',     'pbkdf2$120000$tOavXtrWBeD/0EvU7BsIqw==$7YSe4bthjw5R0SxcI5IKD4cwtgwf6QMXg1gRfnORKuI='),
(5, 'Elisa Souza',     'elisa.souza@exemplo.com',     'pbkdf2$120000$+C5c9SDIqfmfaY1mcYgeTA==$pukTwlS/uENV5Lu00XcF4ECxWJSdJX4/hf5rTdwqsUc='),
(3, 'Fábio Costa',     'fabio.costa@exemplo.com',     'pbkdf2$120000$fipEiFhMYMb0+08kylKakQ==$+uw9jSNOUULpkOhAjy/xCXh8q8MWHLHzDwChPlkTK+c='),
(4, 'Gabriela Alves',  'gabriela.alves@exemplo.com',  'pbkdf2$120000$Mtsp+d/DP4RygDjlZRA1Zw==$k2DX1Fo9orA92OyL5bUwRpCGOLv3ELBUH8V+qJzfInA='),
(7, 'Henrique Dias',   'henrique.dias@exemplo.com',   'pbkdf2$120000$FN9f3UgQUsTGa6Lyu6/tZA==$qi9Pm90AcpN9znbpI+sxMONjgVe5NHlA3ZU1Vfo7eL4='),
(8, 'Isabela Nunes',   'isabela.nunes@exemplo.com',   'pbkdf2$120000$0N5Io++lxf0UhTSQcOj+Cw==$ej+PvQBEyWtYgvyN28vC2rtgfopsyJsRfV3Fil+pJLM='),
(2, 'João Pereira',    'joao.pereira@exemplo.com',    'pbkdf2$120000$ealcXZy45YP/M4Kw0hy4nw==$RBb3vExDLKJa6E+DG3DNHrFqrjoLkRymkn+NBbV//1E=');


-- Redes locais (ids 1–10) ----------------------------------------------
INSERT INTO rede_local (idusuarios, nome_ssid, faixa_ip, gateway) VALUES
(1,  'Casa_Souza',            '192.168.0.0/24',  '192.168.0.1'),
(1,  'Casa_Souza_Convidados', '192.168.10.0/24', '192.168.10.1'),
(2,  'Lima_WiFi',             '192.168.1.0/24',  '192.168.1.1'),
(3,  'Mendes_5G',             '10.0.0.0/24',     '10.0.0.1'),
(4,  'Rocha_Home',            '192.168.2.0/24',  '192.168.2.1'),
(5,  'Elisa_Net',             '192.168.3.0/24',  '192.168.3.1'),
(6,  'Costa_Rede',            '172.16.0.0/24',   '172.16.0.1'),
(8,  'Dias_WiFi',             '192.168.4.0/24',  '192.168.4.1'),
(9,  'Nunes_Casa',            '192.168.5.0/24',  '192.168.5.1'),
(10, 'Pereira_Fibra',         '192.168.6.0/24',  '192.168.6.1');


-- Servidores DNS públicos (ids 1–10) -----------------------------------
INSERT INTO servidor_dns (nome_servidor, ip_primario, ip_secundario, suporta_doh) VALUES
('Google Public DNS', '8.8.8.8',        '8.8.4.4',         TRUE),
('Cloudflare',        '1.1.1.1',        '1.0.0.1',         TRUE),
('Quad9',             '9.9.9.9',        '149.112.112.112', TRUE),
('OpenDNS',           '208.67.222.222', '208.67.220.220',  TRUE),
('AdGuard DNS',       '94.140.14.14',   '94.140.15.15',    TRUE),
('CleanBrowsing',     '185.228.168.9',  '185.228.169.9',   TRUE),
('Comodo Secure DNS', '8.26.56.26',     '8.20.247.20',     FALSE),
('Level3',            '4.2.2.1',        '4.2.2.2',         FALSE),
('Yandex DNS',        '77.88.8.8',      '77.88.8.1',       FALSE),
('Control D',         '76.76.2.0',      '76.76.10.0',      TRUE);


-- Redes x servidores DNS (usam) ----------------------------------------
INSERT INTO usam (id_rede, id_servidor) VALUES
(1, 2), (1, 5), (2, 2), (3, 1), (3, 2), (4, 3), (5, 4), (5, 6),
(6, 6), (7, 1), (8, 10), (9, 3), (9, 7), (10, 2), (10, 8);


-- Dispositivos (12) ----------------------------------------------------
INSERT INTO dispositivo (mac_address, idusuarios, idrede_local, nome_dispositivo, ip_aparelho, tipo_dispositivo, dns) VALUES
('aa:00:00:00:00:01', 1,    1, 'Celular da Ana',        '192.168.0.10',  'celular',  '192.168.0.1'),
('aa:00:00:00:00:02', 1,    1, 'Notebook da Ana',       '192.168.0.11',  'notebook', '192.168.0.1'),
('aa:00:00:00:00:03', NULL, 1, 'Smart TV da sala',      '192.168.0.20',  'smart TV', '192.168.0.1'),
('aa:00:00:00:00:04', 5,    1, 'Tablet da Elisa',       '192.168.0.30',  'tablet',   '192.168.0.1'),
('aa:00:00:00:00:05', 2,    3, 'Celular do Bruno',      '192.168.1.10',  'celular',  '192.168.1.1'),
('aa:00:00:00:00:06', 2,    3, 'PC gamer',              '192.168.1.11',  'desktop',  '192.168.1.1'),
('aa:00:00:00:00:07', NULL, 3, 'Lâmpada inteligente',   '192.168.1.50',  'IoT',      '192.168.1.1'),
('aa:00:00:00:00:08', 3,    4, 'Notebook do trabalho',  '10.0.0.10',     'notebook', '10.0.0.1'),
('aa:00:00:00:00:09', 3,    4, 'Celular da Carla',      '10.0.0.11',     'celular',  '10.0.0.1'),
('aa:00:00:00:00:0a', 4,    5, 'Console',               '192.168.2.10',  'console',  '192.168.2.1'),
('aa:00:00:00:00:0b', NULL, 5, 'Câmera de segurança',   '192.168.2.40',  'IoT',      '192.168.2.1'),
('aa:00:00:00:00:0c', 7,    2, 'Celular da visitante',  '192.168.10.50', 'celular',  '192.168.10.1');


-- Categorias de domínio (ids 1–10) -------------------------------------
INSERT INTO cat_dominio (nome_categoria, nivel_risco) VALUES
('Anúncio',                  'baixo'),    -- 1
('Rastreador',               'medio'),    -- 2
('Malware',                  'critico'),  -- 3
('Phishing',                 'critico'),  -- 4
('Rede social',              'baixo'),    -- 5
('Conteúdo adulto',          'alto'),     -- 6
('Apostas',                  'alto'),     -- 7
('Mineração de criptomoeda', 'alto'),     -- 8
('Telemetria',               'medio'),    -- 9
('Streaming',                'baixo');    -- 10


-- Domínios (ids 1–21) --------------------------------------------------
INSERT INTO dominio (idcat_dominio, nome_dominio) VALUES
(1,    'doubleclick.net'),             -- 1
(1,    'googlesyndication.com'),       -- 2
(1,    'adservice.google.com'),        -- 3
(1,    'ads.yahoo.com'),               -- 4
(2,    'google-analytics.com'),        -- 5
(2,    'hotjar.com'),                  -- 6
(2,    'scorecardresearch.com'),       -- 7
(5,    'facebook.com'),                -- 8
(5,    'tiktok.com'),                  -- 9
(5,    'instagram.com'),               -- 10
(9,    'vortex.data.microsoft.com'),   -- 11
(8,    'coinhive.com'),                -- 12
(3,    'malware-exemplo.test'),        -- 13
(3,    'virus-download.test'),         -- 14
(4,    'banco-falso-login.test'),      -- 15
(4,    'premio-gratis.test'),          -- 16
(6,    'conteudo-adulto.test'),        -- 17
(7,    'apostas-online.test'),         -- 18
(10,   'youtube.com'),                 -- 19
(10,   'netflix.com'),                 -- 20
(NULL, 'wikipedia.org');               -- 21 (sem categoria)


-- Listas de bloqueio (ids 1–10) ----------------------------------------
INSERT INTO lista_bloqueio (nome_lista, ativa, url_origem) VALUES
('StevenBlack Unified',      TRUE,  'https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts'),  -- 1
('EasyList',                 TRUE,  'https://easylist.to/easylist/easylist.txt'),                         -- 2
('EasyPrivacy',              TRUE,  'https://easylist.to/easylist/easyprivacy.txt'),                      -- 3
('AdGuard DNS filter',       TRUE,  'https://adguardteam.github.io/AdGuardSDNSFilter/Filters/filter.txt'),-- 4
('Malware e phishing',       TRUE,  NULL),                                                                -- 5
('Controle parental',        TRUE,  NULL),                                                                -- 6
('Redes sociais',            TRUE,  NULL),                                                                -- 7
('Apostas',                  TRUE,  NULL),                                                                -- 8
('Telemetria',               TRUE,  NULL),                                                                -- 9
('Lista antiga',             FALSE, NULL);                                                               -- 10


-- Domínios x listas (contem) -------------------------------------------
INSERT INTO contem (id_lista, id_dominio) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 11),
(2, 1), (2, 2), (2, 3), (2, 4),
(3, 5), (3, 6), (3, 7), (3, 11),
(4, 1), (4, 2), (4, 3), (4, 4), (4, 5), (4, 12),
(5, 12), (5, 13), (5, 14), (5, 15), (5, 16),
(6, 13), (6, 14), (6, 15), (6, 16), (6, 17), (6, 18),
(7, 8), (7, 9), (7, 10),
(8, 18),
(9, 5), (9, 11),
(10, 1), (10, 21);

-- O trigger trg_contem_atualiza_lista marcou todas as listas com a hora
-- atual ao receberem domínios. Para os dados de exemplo ficarem realistas,
-- voltamos as datas para dias diferentes no passado.
UPDATE lista_bloqueio
   SET data_atualizacao = CURRENT_TIMESTAMP - (id_lista * INTERVAL '3 days')
 WHERE id_lista <= 9;
UPDATE lista_bloqueio
   SET data_atualizacao = CURRENT_TIMESTAMP - INTERVAL '400 days'
 WHERE id_lista = 10;


-- Dispositivos x listas (tem) ------------------------------------------
INSERT INTO tem (mac_address, id_lista) VALUES
('aa:00:00:00:00:01', 1), ('aa:00:00:00:00:01', 5),
('aa:00:00:00:00:02', 1), ('aa:00:00:00:00:02', 5),
('aa:00:00:00:00:03', 2), ('aa:00:00:00:00:03', 5), ('aa:00:00:00:00:03', 9),
('aa:00:00:00:00:04', 1), ('aa:00:00:00:00:04', 5), ('aa:00:00:00:00:04', 6), ('aa:00:00:00:00:04', 7),
('aa:00:00:00:00:05', 4),
('aa:00:00:00:00:06', 4), ('aa:00:00:00:00:06', 8),
('aa:00:00:00:00:07', 9),
('aa:00:00:00:00:08', 5),
('aa:00:00:00:00:09', 3), ('aa:00:00:00:00:09', 7),
('aa:00:00:00:00:0a', 1), ('aa:00:00:00:00:0a', 10),
('aa:00:00:00:00:0b', 5), ('aa:00:00:00:00:0b', 9),
('aa:00:00:00:00:0c', 1), ('aa:00:00:00:00:0c', 5), ('aa:00:00:00:00:0c', 6);


-- Regras personalizadas (10) -------------------------------------------
INSERT INTO regra_personalizada (iddominio, iddispositivo, tipo_acao, motivo) VALUES
(5,  'aa:00:00:00:00:02', 'liberar',  'Necessário para o trabalho'),
(19, 'aa:00:00:00:00:04', 'bloquear', 'Limite de tempo de tela'),
(20, 'aa:00:00:00:00:04', 'bloquear', 'Limite de tempo de tela'),
(9,  'aa:00:00:00:00:06', 'bloquear', 'Distração durante os estudos'),
(8,  'aa:00:00:00:00:08', 'bloquear', 'Foco no trabalho'),
(10, 'aa:00:00:00:00:09', 'liberar',  'Uso profissional do Instagram'),
(18, 'aa:00:00:00:00:0a', 'bloquear', 'Bloqueio de apostas no console'),
(9,  'aa:00:00:00:00:03', 'bloquear', 'TV da sala sem TikTok'),
(1,  'aa:00:00:00:00:01', 'liberar',  'Teste de exibição de anúncios'),
(17, 'aa:00:00:00:00:05', 'bloquear', 'Bloqueio de conteúdo adulto');


-- Requisições DNS ------------------------------------------------------
-- Inseridas pela procedure, que decide o status. O trigger
-- trg_alerta_requisicao gera os alertas das bloqueadas de alto risco.
-- A ordem importa: a segunda consulta do notebook a wikipedia.org cai
-- no cache porque a primeira aconteceu 2 minutos antes.
CALL registrar_requisicao('aa:00:00:00:00:01', 13, 41, (CURRENT_TIMESTAMP - INTERVAL '3 days 2 hours')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:02', 14, 38, (CURRENT_TIMESTAMP - INTERVAL '3 days 1 hour')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:03', 16, 25, (CURRENT_TIMESTAMP - INTERVAL '2 days 20 hours')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:04', 17, 30, (CURRENT_TIMESTAMP - INTERVAL '2 days 9 hours')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:05', 17, 33, (CURRENT_TIMESTAMP - INTERVAL '2 days 4 hours')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:06', 18, 27, (CURRENT_TIMESTAMP - INTERVAL '1 day 22 hours')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:06', 12, 52, (CURRENT_TIMESTAMP - INTERVAL '1 day 21 hours')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:08', 15, 19, (CURRENT_TIMESTAMP - INTERVAL '1 day 15 hours')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:0b', 14, 61, (CURRENT_TIMESTAMP - INTERVAL '1 day 6 hours')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:0c', 18, 22, (CURRENT_TIMESTAMP - INTERVAL '1 day 2 hours')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:0c', 16, 24, (CURRENT_TIMESTAMP - INTERVAL '1 day 1 hour')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:0a', 18, 35, (CURRENT_TIMESTAMP - INTERVAL '20 hours')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:04', 13, 28, (CURRENT_TIMESTAMP - INTERVAL '18 hours')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:01', 15, 31, (CURRENT_TIMESTAMP - INTERVAL '16 hours')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:01', 1,  45, (CURRENT_TIMESTAMP - INTERVAL '15 hours')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:01', 8,  48, (CURRENT_TIMESTAMP - INTERVAL '14 hours')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:02', 5,  36, (CURRENT_TIMESTAMP - INTERVAL '13 hours')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:02', 2,  12, (CURRENT_TIMESTAMP - INTERVAL '12 hours')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:02', 19, 55, (CURRENT_TIMESTAMP - INTERVAL '11 hours')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:03', 9,  20, (CURRENT_TIMESTAMP - INTERVAL '10 hours')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:03', 20, 64, (CURRENT_TIMESTAMP - INTERVAL '9 hours')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:04', 19, 18, (CURRENT_TIMESTAMP - INTERVAL '8 hours')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:05', 1,  15, (CURRENT_TIMESTAMP - INTERVAL '7 hours')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:05', 19, 58, (CURRENT_TIMESTAMP - INTERVAL '6 hours 30 minutes')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:06', 9,  21, (CURRENT_TIMESTAMP - INTERVAL '6 hours')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:07', 11, 14, (CURRENT_TIMESTAMP - INTERVAL '5 hours')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:07', 11, 13, (CURRENT_TIMESTAMP - INTERVAL '4 hours')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:08', 8,  17, (CURRENT_TIMESTAMP - INTERVAL '3 hours 30 minutes')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:08', 21, 42, (CURRENT_TIMESTAMP - INTERVAL '3 hours')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:08', 21, 40, (CURRENT_TIMESTAMP - INTERVAL '2 hours 58 minutes')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:09', 10, 47, (CURRENT_TIMESTAMP - INTERVAL '2 hours 30 minutes')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:09', 5,  16, (CURRENT_TIMESTAMP - INTERVAL '2 hours')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:09', 21, 39, (CURRENT_TIMESTAMP - INTERVAL '1 hour 30 minutes')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:0a', 20, 70, (CURRENT_TIMESTAMP - INTERVAL '1 hour')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:0b', 11, 11, (CURRENT_TIMESTAMP - INTERVAL '40 minutes')::timestamp);
CALL registrar_requisicao('aa:00:00:00:00:0c', 1,  23, (CURRENT_TIMESTAMP - INTERVAL '20 minutes')::timestamp);

-- Os alertas mais antigos já foram vistos pelo administrador.
UPDATE alertas_seguranca SET visualizado = TRUE WHERE id_alerta <= 5;
