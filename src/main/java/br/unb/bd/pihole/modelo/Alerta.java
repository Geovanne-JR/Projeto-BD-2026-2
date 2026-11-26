package br.unb.bd.pihole.modelo;

import java.time.LocalDateTime;

/**
 * Linha da tabela alertas_seguranca, com dados da requisição de origem
 * (via JOIN). Os alertas são criados pelo trigger trg_alerta_requisicao.
 *
 * <pre>
 * id_alerta      INT (identity)   -> id
 * idrequisicoes  INT (FK)         -> idRequisicao
 * visualizado    BOOLEAN          -> visualizado
 * mensagem       VARCHAR(500)     -> mensagem
 * gravidade      gravidade_enum   -> gravidade (texto)
 * data_geracao   TIMESTAMP        -> dataGeracao
 * </pre>
 */
public record Alerta(int id, int idRequisicao, boolean visualizado, String mensagem,
                     String gravidade, LocalDateTime dataGeracao,
                     String nomeDispositivo, String dominio) {
}
