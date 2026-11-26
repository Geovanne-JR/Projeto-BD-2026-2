package br.unb.bd.pihole.modelo;

import java.time.LocalDateTime;

/**
 * Linha da view vw_historico_requisicoes (requisicoes + dispositivo +
 * usuarios + dominio + cat_dominio). Só leitura: as requisições são
 * gravadas pela procedure registrar_requisicao.
 */
public record RegistroRequisicao(int id, LocalDateTime dataHora, String mac,
                                 String nomeDispositivo, String dono, String dominio,
                                 String categoria, String nivelRisco, String status,
                                 Integer tempoRespostaMs) {
}
