-- =====================================================================
-- Projeto de Banco de Dados — Entrega final
-- View, Procedure e Triggers
-- Executar DEPOIS de schema.sql.
-- =====================================================================


-- ---------------------------------------------------------------------
-- VIEW: histórico de requisições já "traduzido" para leitura humana.
-- Junta 5 tabelas (requisicoes, dispositivo, usuarios, dominio,
-- cat_dominio). Usada pela aba "Histórico DNS" da aplicação.
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW vw_historico_requisicoes AS
SELECT r.id_requisicao,
       r.data_hora,
       r.iddispositivo      AS mac_address,
       d.nome_dispositivo,
       u.nome               AS dono,
       dm.nome_dominio,
       c.nome_categoria,
       c.nivel_risco,
       r.status,
       r.tempo_resposta_ms
FROM requisicoes r
JOIN dispositivo d       ON d.mac_address  = r.iddispositivo
LEFT JOIN usuarios u     ON u.id_usuario   = d.idusuarios
JOIN dominio dm          ON dm.id_dominio  = r.iddominio
LEFT JOIN cat_dominio c  ON c.id_categoria = dm.idcat_dominio;


-- ---------------------------------------------------------------------
-- PROCEDURE: registra uma requisição DNS decidindo o status, do mesmo
-- jeito que o Pi-hole faria:
--   1. regra personalizada do dispositivo para o domínio tem prioridade
--      ('bloquear' => bloqueada, 'liberar' => permitida);
--   2. sem regra, o domínio é bloqueado se estiver em alguma lista ATIVA
--      associada ao dispositivo;
--   3. se foi permitida há menos de 5 minutos, a resposta vem do cache.
-- Usada pela aba "Histórico DNS" (botão "Simular requisição") e pela
-- carga de dados (dados.sql).
-- ---------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE registrar_requisicao(
    p_mac         MACADDR,
    p_id_dominio  INT,
    p_tempo_ms    INT       DEFAULT NULL,
    p_data_hora   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_acao    VARCHAR(10);
    v_status  status_req_enum := 'permitida';
BEGIN
    SELECT tipo_acao INTO v_acao
    FROM regra_personalizada
    WHERE iddominio = p_id_dominio
      AND iddispositivo = p_mac;

    IF v_acao = 'bloquear' THEN
        v_status := 'bloqueada';
    ELSIF v_acao IS NULL AND EXISTS (
        SELECT 1
        FROM tem t
        JOIN lista_bloqueio l ON l.id_lista = t.id_lista AND l.ativa
        JOIN contem c         ON c.id_lista = t.id_lista
        WHERE t.mac_address = p_mac
          AND c.id_dominio  = p_id_dominio
    ) THEN
        v_status := 'bloqueada';
    ELSIF EXISTS (
        SELECT 1
        FROM requisicoes
        WHERE iddispositivo = p_mac
          AND iddominio     = p_id_dominio
          AND status       <> 'bloqueada'
          AND data_hora BETWEEN p_data_hora - INTERVAL '5 minutes' AND p_data_hora
    ) THEN
        v_status := 'cache';
    END IF;

    INSERT INTO requisicoes (iddispositivo, iddominio, data_hora, status, tempo_resposta_ms)
    VALUES (p_mac, p_id_dominio, p_data_hora, v_status,
            CASE WHEN v_status = 'cache' THEN 0 ELSE p_tempo_ms END);
END;
$$;


-- ---------------------------------------------------------------------
-- TRIGGER 1: gera um alerta de segurança quando uma requisição a um
-- domínio de risco ALTO ou CRÍTICO é bloqueada.
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_gerar_alerta()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_risco        nivel_risco_enum;
    v_categoria    VARCHAR(50);
    v_dominio      VARCHAR(253);
    v_dispositivo  VARCHAR(100);
BEGIN
    IF NEW.status <> 'bloqueada' THEN
        RETURN NEW;
    END IF;

    SELECT c.nivel_risco, c.nome_categoria, dm.nome_dominio
      INTO v_risco, v_categoria, v_dominio
    FROM dominio dm
    LEFT JOIN cat_dominio c ON c.id_categoria = dm.idcat_dominio
    WHERE dm.id_dominio = NEW.iddominio;

    IF v_risco IN ('alto', 'critico') THEN
        SELECT nome_dispositivo INTO v_dispositivo
        FROM dispositivo
        WHERE mac_address = NEW.iddispositivo;

        INSERT INTO alertas_seguranca (idrequisicoes, mensagem, gravidade, data_geracao)
        VALUES (NEW.id_requisicao,
                format('O dispositivo "%s" tentou acessar %s (%s). A requisição foi bloqueada.',
                       v_dispositivo, v_dominio, v_categoria),
                CASE v_risco WHEN 'critico' THEN 'critica' ELSE 'alta' END::gravidade_enum,
                NEW.data_hora);
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_alerta_requisicao ON requisicoes;
CREATE TRIGGER trg_alerta_requisicao
    AFTER INSERT ON requisicoes
    FOR EACH ROW
    EXECUTE FUNCTION fn_gerar_alerta();


-- ---------------------------------------------------------------------
-- TRIGGER 2: mantém lista_bloqueio.data_atualizacao em dia sempre que
-- um domínio entra ou sai da lista (tabela associativa contem).
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_atualizar_data_lista()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        UPDATE lista_bloqueio SET data_atualizacao = CURRENT_TIMESTAMP WHERE id_lista = OLD.id_lista;
        RETURN OLD;
    END IF;
    UPDATE lista_bloqueio SET data_atualizacao = CURRENT_TIMESTAMP WHERE id_lista = NEW.id_lista;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_contem_atualiza_lista ON contem;
CREATE TRIGGER trg_contem_atualiza_lista
    AFTER INSERT OR DELETE ON contem
    FOR EACH ROW
    EXECUTE FUNCTION fn_atualizar_data_lista();
