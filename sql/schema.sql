-- =====================================================================
-- Projeto de Banco de Dados — Entrega 1
-- Sistema de Monitoramento de Rede Doméstica e Bloqueio de Anúncios via DNS
-- UnB / CIC — Banco de Dados — 2026/2
-- =====================================================================


-- ---------------------------------------------------------------------
-- Limpeza
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS alertas_seguranca   CASCADE;
DROP TABLE IF EXISTS requisicoes         CASCADE;
DROP TABLE IF EXISTS regra_personalizada CASCADE;
DROP TABLE IF EXISTS tem                 CASCADE;
DROP TABLE IF EXISTS contem              CASCADE;
DROP TABLE IF EXISTS lista_bloqueio      CASCADE;
DROP TABLE IF EXISTS dominio             CASCADE;
DROP TABLE IF EXISTS cat_dominio         CASCADE;
DROP TABLE IF EXISTS dispositivo         CASCADE;
DROP TABLE IF EXISTS usam                CASCADE;
DROP TABLE IF EXISTS servidor_dns        CASCADE;
DROP TABLE IF EXISTS rede_local          CASCADE;
DROP TABLE IF EXISTS usuarios            CASCADE;
DROP TABLE IF EXISTS perfis              CASCADE;

DROP TYPE IF EXISTS nivel_risco_enum CASCADE;
DROP TYPE IF EXISTS gravidade_enum   CASCADE;
DROP TYPE IF EXISTS status_req_enum  CASCADE;


-- ---------------------------------------------------------------------
-- Tipos enumerados
-- ---------------------------------------------------------------------
CREATE TYPE nivel_risco_enum AS ENUM ('baixo', 'medio', 'alto', 'critico');
CREATE TYPE gravidade_enum   AS ENUM ('baixa', 'media', 'alta', 'critica');
CREATE TYPE status_req_enum  AS ENUM ('permitida', 'bloqueada', 'cache');


-- =====================================================================
-- ENTIDADES
-- =====================================================================

-- Perfis de acesso dos usuários
CREATE TABLE perfis (
    id_perfil   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nomeperfil  VARCHAR(50)  NOT NULL UNIQUE,
    descricao   VARCHAR(255)
);

-- Usuários do sistema
CREATE TABLE usuarios (
    id_usuario   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    idperfis     INT          NOT NULL,
    nome         VARCHAR(100) NOT NULL,
    email        VARCHAR(150) NOT NULL UNIQUE,
    foto_perfil  BYTEA,                       -- dado binário (imagem)
    senha_hash   VARCHAR(255) NOT NULL,
    CONSTRAINT fk_usuarios_perfis
        FOREIGN KEY (idperfis) REFERENCES perfis (id_perfil)
);

-- Redes locais
CREATE TABLE rede_local (
    id_rede     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    idusuarios  INT          NOT NULL,
    nome_ssid   VARCHAR(32)  NOT NULL,        -- SSID tem no máximo 32 bytes
    faixa_ip    CIDR         NOT NULL,        -- ex.: 192.168.0.0/24
    gateway     INET         NOT NULL,        -- ex.: 192.168.0.1
    CONSTRAINT fk_rede_usuarios
        FOREIGN KEY (idusuarios) REFERENCES usuarios (id_usuario)
);

-- Servidores DNS externos
CREATE TABLE servidor_dns (
    id_servidor    INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome_servidor  VARCHAR(100) NOT NULL,
    ip_primario    INET         NOT NULL,
    ip_secundario  INET,
    suporta_doh    BOOLEAN      NOT NULL DEFAULT FALSE  -- DNS over HTTPS
);

-- Dispositivos conectados
CREATE TABLE dispositivo (
    mac_address       MACADDR      PRIMARY KEY,
    idusuarios        INT,                    -- (0,1): dispositivo pode não ter dono
    idrede_local      INT          NOT NULL,
    nome_dispositivo  VARCHAR(100) NOT NULL,
    ip_aparelho       INET,
    tipo_dispositivo  VARCHAR(50),            -- ex.: celular, notebook, smart TV, IoT
    dns               INET,                   -- DNS configurado no aparelho
    CONSTRAINT fk_dispositivo_usuarios
        FOREIGN KEY (idusuarios) REFERENCES usuarios (id_usuario)
        ON DELETE SET NULL,
    CONSTRAINT fk_dispositivo_rede
        FOREIGN KEY (idrede_local) REFERENCES rede_local (id_rede)
);

-- Categorias de domínio
CREATE TABLE cat_dominio (
    id_categoria    INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome_categoria  VARCHAR(50)      NOT NULL UNIQUE,  -- ex.: anúncio, rastreador, malware
    nivel_risco     nivel_risco_enum NOT NULL
);

-- Domínios
CREATE TABLE dominio (
    id_dominio      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    idcat_dominio   INT,                      -- (0,1): domínio pode estar sem categoria
    nome_dominio    VARCHAR(253) NOT NULL UNIQUE,  -- 253 = tamanho máximo de um domínio
    CONSTRAINT fk_dominio_categoria
        FOREIGN KEY (idcat_dominio) REFERENCES cat_dominio (id_categoria)
        ON DELETE SET NULL
);

-- Listas de bloqueio
CREATE TABLE lista_bloqueio (
    id_lista           INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome_lista         VARCHAR(100) NOT NULL,
    data_atualizacao   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ativa              BOOLEAN      NOT NULL DEFAULT TRUE,
    url_origem         VARCHAR(500)
);

-- Regras
CREATE TABLE regra_personalizada (
    id_regra       INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    iddominio      INT          NOT NULL,
    iddispositivo  MACADDR      NOT NULL,
    motivo         VARCHAR(255),
    data_criacao   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo_acao      VARCHAR(10)  NOT NULL,
    CONSTRAINT ck_regra_tipo_acao
        CHECK (tipo_acao IN ('bloquear', 'liberar')),
    CONSTRAINT uq_regra_dominio_dispositivo     -- uma regra por par domínio/dispositivo
        UNIQUE (iddominio, iddispositivo),
    CONSTRAINT fk_regra_dominio
        FOREIGN KEY (iddominio) REFERENCES dominio (id_dominio)
        ON DELETE CASCADE,
    CONSTRAINT fk_regra_dispositivo
        FOREIGN KEY (iddispositivo) REFERENCES dispositivo (mac_address)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- Histórico de requisições
CREATE TABLE requisicoes (
    id_requisicao      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    iddispositivo      MACADDR          NOT NULL,
    iddominio          INT              NOT NULL,
    data_hora          TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status             status_req_enum  NOT NULL,
    tempo_resposta_ms  INT,
    CONSTRAINT ck_req_tempo
        CHECK (tempo_resposta_ms IS NULL OR tempo_resposta_ms >= 0),
    CONSTRAINT fk_req_dispositivo
        FOREIGN KEY (iddispositivo) REFERENCES dispositivo (mac_address)
        ON UPDATE CASCADE,
    CONSTRAINT fk_req_dominio
        FOREIGN KEY (iddominio) REFERENCES dominio (id_dominio)
);

-- Alertas de segurança
CREATE TABLE alertas_seguranca (
    id_alerta       INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    idrequisicoes   INT             NOT NULL,
    visualizado     BOOLEAN         NOT NULL DEFAULT FALSE,
    mensagem        VARCHAR(500)    NOT NULL,
    gravidade       gravidade_enum  NOT NULL,
    data_geracao    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_alerta_requisicao
        FOREIGN KEY (idrequisicoes) REFERENCES requisicoes (id_requisicao)
        ON DELETE CASCADE
);


-- =====================================================================
-- TABELAS ASSOCIATIVAS
-- =====================================================================

-- usam
CREATE TABLE usam (
    id_rede      INT NOT NULL,
    id_servidor  INT NOT NULL,
    CONSTRAINT pk_usam PRIMARY KEY (id_rede, id_servidor),
    CONSTRAINT fk_usam_rede
        FOREIGN KEY (id_rede) REFERENCES rede_local (id_rede)
        ON DELETE CASCADE,
    CONSTRAINT fk_usam_servidor
        FOREIGN KEY (id_servidor) REFERENCES servidor_dns (id_servidor)
        ON DELETE CASCADE
);

-- contem
CREATE TABLE contem (
    id_dominio  INT NOT NULL,
    id_lista    INT NOT NULL,
    CONSTRAINT pk_contem PRIMARY KEY (id_dominio, id_lista),
    CONSTRAINT fk_contem_dominio
        FOREIGN KEY (id_dominio) REFERENCES dominio (id_dominio)
        ON DELETE CASCADE,
    CONSTRAINT fk_contem_lista
        FOREIGN KEY (id_lista) REFERENCES lista_bloqueio (id_lista)
        ON DELETE CASCADE
);

-- tem:
CREATE TABLE tem (
    id_lista     INT     NOT NULL,
    mac_address  MACADDR NOT NULL,
    CONSTRAINT pk_tem PRIMARY KEY (id_lista, mac_address),
    CONSTRAINT fk_tem_lista
        FOREIGN KEY (id_lista) REFERENCES lista_bloqueio (id_lista)
        ON DELETE CASCADE,
    CONSTRAINT fk_tem_dispositivo
        FOREIGN KEY (mac_address) REFERENCES dispositivo (mac_address)
        ON UPDATE CASCADE ON DELETE CASCADE
);


-- =====================================================================
-- ÍNDICES nas chaves estrangeiras mais consultadas
-- =====================================================================
CREATE INDEX idx_usuarios_perfil        ON usuarios (idperfis);
CREATE INDEX idx_rede_usuario           ON rede_local (idusuarios);
CREATE INDEX idx_dispositivo_usuario    ON dispositivo (idusuarios);
CREATE INDEX idx_dispositivo_rede       ON dispositivo (idrede_local);
CREATE INDEX idx_dominio_categoria      ON dominio (idcat_dominio);
CREATE INDEX idx_regra_dispositivo      ON regra_personalizada (iddispositivo);
CREATE INDEX idx_req_dispositivo        ON requisicoes (iddispositivo);
CREATE INDEX idx_req_dominio            ON requisicoes (iddominio);
CREATE INDEX idx_req_data_hora          ON requisicoes (data_hora);
CREATE INDEX idx_alerta_requisicao      ON alertas_seguranca (idrequisicoes);
CREATE INDEX idx_contem_lista           ON contem (id_lista);
CREATE INDEX idx_tem_dispositivo        ON tem (mac_address);
CREATE INDEX idx_usam_servidor          ON usam (id_servidor);
