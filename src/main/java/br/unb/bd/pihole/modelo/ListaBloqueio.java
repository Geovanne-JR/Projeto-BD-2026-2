package br.unb.bd.pihole.modelo;

import java.time.LocalDateTime;

/**
 * Linha da tabela lista_bloqueio.
 *
 * <pre>
 * id_lista          INT (identity)  -> id
 * nome_lista        VARCHAR(100)    -> nome
 * data_atualizacao  TIMESTAMP       -> dataAtualizacao (mantida pelo banco)
 * ativa             BOOLEAN         -> ativa
 * url_origem        VARCHAR(500)    -> urlOrigem
 * </pre>
 */
public record ListaBloqueio(Integer id, String nome, LocalDateTime dataAtualizacao,
                            boolean ativa, String urlOrigem) {

    @Override
    public String toString() {
        return ativa ? nome : nome + " (desativada)";
    }
}
