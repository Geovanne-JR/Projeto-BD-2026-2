-- =============================================================================
--  Projeto de Banco de Dados - UnB / CIC - 2026/2
--  Tema   : Sistema de Monitoramento de Rede Doméstica e Bloqueio de
--           Anúncios via DNS (inspirado no Pi-hole)
--  SGBD   : PostgreSQL 14+
--  Repo   : https://github.com/Geovanne-JR/Projeto-BD-2026-2
--
--  Script DDL - cria o esquema completo (11 entidades + 3 tabelas associativas).
--  Pode ser executado várias vezes: as tabelas são removidas antes de recriadas.
--
--  Execução:
--     createdb pihole_bd
--     psql -U postgres -d pihole_bd -f sql/schema.sql
-- =============================================================================

SET client_encoding = 'UTF8';

BEGIN;

-- -----------------------------------------------------------------------------
-- Limpeza (ordem inversa das dependências)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS alerta_seguranca     CASCADE;
DROP TABLE IF EXISTS requisicao_dns       CASCADE;
DROP TABLE IF EXISTS regra_personalizada  CASCADE;
DROP TABLE IF EXISTS dispositivo_lista    CASCADE;
DROP TABLE IF EXISTS lista_dominio        CASCADE;
DROP TABLE IF EXISTS lista_bloqueio       CASCADE;
DROP TABLE IF EXISTS dominio              CASCADE;
DROP TABLE IF EXISTS categoria_dominio    CASCADE;
DROP TABLE IF EXISTS rede_servidor_dns    CASCADE;
DROP TABLE IF EXISTS servidor_dns         CASCADE;
DROP TABLE IF EXISTS dispositivo          CASCADE;
DROP TABLE IF EXISTS rede_local           CASCADE;
DROP TABLE IF EXISTS usuario              CASCADE;
DROP TABLE IF EXISTS perfil               CASCADE;


-- =============================================================================
-- 1. PERFIL - papéis de acesso ao sistema (Administrador, Responsável, Visitante...)
-- =============================================================================
CREATE TABLE perfil (
    id_perfil    INTEGER      GENERATED ALWAYS AS IDENTITY,
    nome_perfil  VARCHAR(50)  NOT NULL,
    descricao    VARCHAR(255),

    CONSTRAINT pk_perfil        PRIMARY KEY (id_perfil),
    CONSTRAINT uq_perfil_nome   UNIQUE (nome_perfil),
    CONSTRAINT ck_perfil_nome   CHECK (length(trim(nome_perfil)) > 0)
);


-- =============================================================================
-- 2. USUARIO - pessoas que administram a rede / possuem dispositivos
--    foto_perfil (BYTEA) atende ao requisito de armazenamento de dado binário.
-- =============================================================================
CREATE TABLE usuario (
    id_usuario   INTEGER       GENERATED ALWAYS AS IDENTITY,
    nome         VARCHAR(100)  NOT NULL,
    email        VARCHAR(150)  NOT NULL,
    senha_hash   VARCHAR(255)  NOT NULL,
    foto_perfil  BYTEA,
    id_perfil    INTEGER       NOT NULL,

    CONSTRAINT pk_usuario         PRIMARY KEY (id_usuario),
    CONSTRAINT uq_usuario_email   UNIQUE (email),
    CONSTRAINT ck_usuario_email   CHECK (email ~* '^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$'),
    CONSTRAINT ck_usuario_nome    CHECK (length(trim(nome)) > 0),
    CONSTRAINT fk_usuario_perfil  FOREIGN KEY (id_perfil)
        REFERENCES perfil (id_perfil)
        ON UPDATE CASCADE ON DELETE RESTRICT
);


-- =============================================================================
-- 3. REDE_LOCAL - rede doméstica (Wi-Fi/LAN) administrada por um usuário
-- =============================================================================
CREATE TABLE rede_local (
    id_rede     INTEGER      GENERATED ALWAYS AS IDENTITY,
    nome_ssid   VARCHAR(32)  NOT NULL,              -- SSID tem no máx. 32 bytes (IEEE 802.11)
    faixa_ip    CIDR         NOT NULL,              -- ex.: 192.168.0.0/24
    gateway     INET         NOT NULL,              -- ex.: 192.168.0.1
    id_usuario  INTEGER      NOT NULL,              -- administrador da rede

    CONSTRAINT pk_rede_local          PRIMARY KEY (id_rede),
    CONSTRAINT uq_rede_local_faixa    UNIQUE (faixa_ip),
    CONSTRAINT ck_rede_gateway_faixa  CHECK (gateway << faixa_ip),  -- gateway deve estar dentro da faixa
    CONSTRAINT fk_rede_local_usuario  FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario)
        ON UPDATE CASCADE ON DELETE RESTRICT
);


-- =============================================================================
-- 4. DISPOSITIVO - aparelhos conectados à rede (celular, notebook, Smart TV...)
--    Chave primária natural: endereço MAC (formato AA:BB:CC:DD:EE:FF).
--    id_usuario é opcional: dispositivos desconhecidos podem ser detectados na rede.
-- =============================================================================
CREATE TABLE dispositivo (
    mac_address       VARCHAR(17)  NOT NULL,
    nome_dispositivo  VARCHAR(100) NOT NULL,
    ip_aparelho       INET         NOT NULL,
    tipo_dispositivo  VARCHAR(20)  NOT NULL DEFAULT 'OUTRO',
    dns_configurado   INET,
    id_usuario        INTEGER,
    id_rede           INTEGER      NOT NULL,

    CONSTRAINT pk_dispositivo          PRIMARY KEY (mac_address),
    CONSTRAINT uq_dispositivo_ip_rede  UNIQUE (id_rede, ip_aparelho),
    CONSTRAINT ck_dispositivo_mac      CHECK (mac_address ~ '^([0-9A-F]{2}:){5}[0-9A-F]{2}$'),
    CONSTRAINT ck_dispositivo_tipo     CHECK (tipo_dispositivo IN (
        'CELULAR', 'NOTEBOOK', 'DESKTOP', 'TABLET', 'SMART_TV',
        'CONSOLE', 'IOT', 'RASPBERRY_PI', 'OUTRO')),
    CONSTRAINT fk_dispositivo_usuario  FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_dispositivo_rede     FOREIGN KEY (id_rede)
        REFERENCES rede_local (id_rede)
        ON UPDATE CASCADE ON DELETE RESTRICT
);


-- =============================================================================
-- 5. SERVIDOR_DNS - servidores upstream (Cloudflare, Google, Quad9, Pi-hole local...)
-- =============================================================================
CREATE TABLE servidor_dns (
    id_servidor    INTEGER      GENERATED ALWAYS AS IDENTITY,
    nome_servidor  VARCHAR(100) NOT NULL,
    ip_primario    INET         NOT NULL,
    ip_secundario  INET,
    suporta_doh    BOOLEAN      NOT NULL DEFAULT FALSE,   -- DNS over HTTPS

    CONSTRAINT pk_servidor_dns          PRIMARY KEY (id_servidor),
    CONSTRAINT uq_servidor_dns_nome     UNIQUE (nome_servidor),
    CONSTRAINT uq_servidor_dns_ip       UNIQUE (ip_primario),
    CONSTRAINT ck_servidor_dns_ips      CHECK (ip_secundario IS NULL OR ip_secundario <> ip_primario)
);


-- -----------------------------------------------------------------------------
-- 5a. REDE_SERVIDOR_DNS - associativa N:N  (rede_local  <-> servidor_dns)
--     Uma rede usa 1..n servidores, em ordem de prioridade.
-- -----------------------------------------------------------------------------
CREATE TABLE rede_servidor_dns (
    id_rede      INTEGER   NOT NULL,
    id_servidor  INTEGER   NOT NULL,
    prioridade   SMALLINT  NOT NULL DEFAULT 1,

    CONSTRAINT pk_rede_servidor_dns         PRIMARY KEY (id_rede, id_servidor),
    CONSTRAINT uq_rede_servidor_prioridade  UNIQUE (id_rede, prioridade),
    CONSTRAINT ck_rede_servidor_prioridade  CHECK (prioridade >= 1),
    CONSTRAINT fk_rsd_rede      FOREIGN KEY (id_rede)
        REFERENCES rede_local (id_rede)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_rsd_servidor  FOREIGN KEY (id_servidor)
        REFERENCES servidor_dns (id_servidor)
        ON UPDATE CASCADE ON DELETE CASCADE
);


-- =============================================================================
-- 6. CATEGORIA_DOMINIO - classificação (Publicidade, Rastreador, Malware, Social...)
-- =============================================================================
CREATE TABLE categoria_dominio (
    id_categoria    INTEGER      GENERATED ALWAYS AS IDENTITY,
    nome_categoria  VARCHAR(50)  NOT NULL,
    nivel_risco     VARCHAR(10)  NOT NULL DEFAULT 'BAIXO',

    CONSTRAINT pk_categoria_dominio  PRIMARY KEY (id_categoria),
    CONSTRAINT uq_categoria_nome     UNIQUE (nome_categoria),
    CONSTRAINT ck_categoria_risco    CHECK (nivel_risco IN ('NENHUM', 'BAIXO', 'MEDIO', 'ALTO', 'CRITICO'))
);


-- =============================================================================
-- 7. DOMINIO - nomes de domínio consultados / bloqueados (ex.: ads.exemplo.com)
--    id_categoria é opcional: domínios novos chegam sem classificação.
-- =============================================================================
CREATE TABLE dominio (
    id_dominio    INTEGER       GENERATED ALWAYS AS IDENTITY,
    nome_dominio  VARCHAR(253)  NOT NULL,           -- 253 = tamanho máx. de um FQDN
    id_categoria  INTEGER,

    CONSTRAINT pk_dominio            PRIMARY KEY (id_dominio),
    CONSTRAINT uq_dominio_nome       UNIQUE (nome_dominio),
    CONSTRAINT ck_dominio_formato    CHECK (nome_dominio ~ '^([a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?\.)+[a-z]{2,63}$'),
    CONSTRAINT fk_dominio_categoria  FOREIGN KEY (id_categoria)
        REFERENCES categoria_dominio (id_categoria)
        ON UPDATE CASCADE ON DELETE SET NULL
);


-- =============================================================================
-- 8. LISTA_BLOQUEIO - listas de bloqueio (StevenBlack, EasyList, lista manual...)
-- =============================================================================
CREATE TABLE lista_bloqueio (
    id_lista          INTEGER       GENERATED ALWAYS AS IDENTITY,
    nome_lista        VARCHAR(100)  NOT NULL,
    url_origem        VARCHAR(500),                 -- NULL = lista criada manualmente
    data_atualizacao  TIMESTAMP,
    ativa             BOOLEAN       NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_lista_bloqueio  PRIMARY KEY (id_lista),
    CONSTRAINT uq_lista_nome      UNIQUE (nome_lista),
    CONSTRAINT ck_lista_url       CHECK (url_origem IS NULL OR url_origem ~* '^https?://')
);


-- -----------------------------------------------------------------------------
-- 8a. LISTA_DOMINIO - associativa N:N  (lista_bloqueio <-> dominio)
-- -----------------------------------------------------------------------------
CREATE TABLE lista_dominio (
    id_lista       INTEGER    NOT NULL,
    id_dominio     INTEGER    NOT NULL,
    data_inclusao  TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_lista_dominio  PRIMARY KEY (id_lista, id_dominio),
    CONSTRAINT fk_ld_lista    FOREIGN KEY (id_lista)
        REFERENCES lista_bloqueio (id_lista)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_ld_dominio  FOREIGN KEY (id_dominio)
        REFERENCES dominio (id_dominio)
        ON UPDATE CASCADE ON DELETE CASCADE
);


-- -----------------------------------------------------------------------------
-- 8b. DISPOSITIVO_LISTA - associativa N:N  (dispositivo <-> lista_bloqueio)
--     Define quais listas de bloqueio se aplicam a cada dispositivo.
-- -----------------------------------------------------------------------------
CREATE TABLE dispositivo_lista (
    mac_address   VARCHAR(17)  NOT NULL,
    id_lista      INTEGER      NOT NULL,
    data_vinculo  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_dispositivo_lista  PRIMARY KEY (mac_address, id_lista),
    CONSTRAINT fk_dl_dispositivo  FOREIGN KEY (mac_address)
        REFERENCES dispositivo (mac_address)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_dl_lista        FOREIGN KEY (id_lista)
        REFERENCES lista_bloqueio (id_lista)
        ON UPDATE CASCADE ON DELETE CASCADE
);


-- =============================================================================
-- 9. REGRA_PERSONALIZADA - exceções por dispositivo (bloquear/permitir um domínio)
--    Ex.: liberar "youtube.com" só no notebook, bloquear "tiktok.com" no tablet.
-- =============================================================================
CREATE TABLE regra_personalizada (
    id_regra      INTEGER       GENERATED ALWAYS AS IDENTITY,
    tipo_acao     VARCHAR(10)   NOT NULL,
    motivo        VARCHAR(255),
    data_criacao  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    mac_address   VARCHAR(17)   NOT NULL,
    id_dominio    INTEGER       NOT NULL,

    CONSTRAINT pk_regra_personalizada  PRIMARY KEY (id_regra),
    CONSTRAINT uq_regra_disp_dominio   UNIQUE (mac_address, id_dominio),   -- 1 regra por par
    CONSTRAINT ck_regra_tipo_acao      CHECK (tipo_acao IN ('BLOQUEAR', 'PERMITIR')),
    CONSTRAINT fk_regra_dispositivo    FOREIGN KEY (mac_address)
        REFERENCES dispositivo (mac_address)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_regra_dominio        FOREIGN KEY (id_dominio)
        REFERENCES dominio (id_dominio)
        ON UPDATE CASCADE ON DELETE CASCADE
);


-- =============================================================================
-- 10. REQUISICAO_DNS - histórico (log) de consultas DNS feitas pelos dispositivos
--     Tabela de maior volume: usa BIGINT e índices de apoio às consultas.
-- =============================================================================
CREATE TABLE requisicao_dns (
    id_requisicao      BIGINT       GENERATED ALWAYS AS IDENTITY,
    data_hora          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status             VARCHAR(10)  NOT NULL,
    tempo_resposta_ms  INTEGER      NOT NULL DEFAULT 0,
    mac_address        VARCHAR(17)  NOT NULL,
    id_dominio         INTEGER      NOT NULL,

    CONSTRAINT pk_requisicao_dns        PRIMARY KEY (id_requisicao),
    CONSTRAINT ck_requisicao_status     CHECK (status IN ('PERMITIDA', 'BLOQUEADA', 'CACHE', 'FALHA')),
    CONSTRAINT ck_requisicao_tempo      CHECK (tempo_resposta_ms >= 0),
    CONSTRAINT fk_requisicao_dispositivo FOREIGN KEY (mac_address)
        REFERENCES dispositivo (mac_address)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_requisicao_dominio    FOREIGN KEY (id_dominio)
        REFERENCES dominio (id_dominio)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE INDEX ix_requisicao_mac_data  ON requisicao_dns (mac_address, data_hora DESC);
CREATE INDEX ix_requisicao_dominio   ON requisicao_dns (id_dominio);
CREATE INDEX ix_requisicao_status    ON requisicao_dns (status);


-- =============================================================================
-- 11. ALERTA_SEGURANCA - alerta gerado a partir de uma requisição suspeita
--     Relacionamento 1:1 com requisicao_dns (UNIQUE na FK).
-- =============================================================================
CREATE TABLE alerta_seguranca (
    id_alerta      INTEGER      GENERATED ALWAYS AS IDENTITY,
    gravidade      VARCHAR(10)  NOT NULL,
    mensagem       TEXT         NOT NULL,
    visualizado    BOOLEAN      NOT NULL DEFAULT FALSE,
    data_geracao   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_requisicao  BIGINT       NOT NULL,

    CONSTRAINT pk_alerta_seguranca   PRIMARY KEY (id_alerta),
    CONSTRAINT uq_alerta_requisicao  UNIQUE (id_requisicao),
    CONSTRAINT ck_alerta_gravidade   CHECK (gravidade IN ('BAIXA', 'MEDIA', 'ALTA', 'CRITICA')),
    CONSTRAINT fk_alerta_requisicao  FOREIGN KEY (id_requisicao)
        REFERENCES requisicao_dns (id_requisicao)
        ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX ix_alerta_nao_visualizado ON alerta_seguranca (visualizado) WHERE visualizado = FALSE;


-- =============================================================================
-- Comentários de documentação (visíveis em \d+ e em ferramentas de modelagem)
-- =============================================================================
COMMENT ON TABLE perfil              IS 'Papéis de acesso dos usuários do sistema';
COMMENT ON TABLE usuario             IS 'Usuários do sistema; foto_perfil armazena imagem binária (BYTEA)';
COMMENT ON TABLE rede_local          IS 'Redes domésticas monitoradas';
COMMENT ON TABLE dispositivo         IS 'Aparelhos conectados à rede, identificados pelo endereço MAC';
COMMENT ON TABLE servidor_dns        IS 'Servidores DNS upstream';
COMMENT ON TABLE rede_servidor_dns   IS 'Associativa N:N rede_local x servidor_dns, com prioridade';
COMMENT ON TABLE categoria_dominio   IS 'Categorias de domínio e respectivo nível de risco';
COMMENT ON TABLE dominio             IS 'Domínios (FQDN) consultados ou bloqueados';
COMMENT ON TABLE lista_bloqueio      IS 'Listas de bloqueio (públicas ou manuais)';
COMMENT ON TABLE lista_dominio       IS 'Associativa N:N lista_bloqueio x dominio';
COMMENT ON TABLE dispositivo_lista   IS 'Associativa N:N dispositivo x lista_bloqueio';
COMMENT ON TABLE regra_personalizada IS 'Exceções de bloqueio/permissão por dispositivo e domínio';
COMMENT ON TABLE requisicao_dns      IS 'Log de consultas DNS realizadas pelos dispositivos';
COMMENT ON TABLE alerta_seguranca    IS 'Alertas de segurança gerados a partir de requisições suspeitas';

COMMIT;
