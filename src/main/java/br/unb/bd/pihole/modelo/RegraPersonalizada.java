package br.unb.bd.pihole.modelo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Linha da tabela regra_personalizada.
 *
 * <pre>
 * id_regra       INT (identity)  -> id
 * iddominio      INT (FK)        -> idDominio
 * iddispositivo  MACADDR (FK)    -> mac
 * motivo         VARCHAR(255)    -> motivo
 * data_criacao   TIMESTAMP       -> dataCriacao (preenchida pelo banco)
 * tipo_acao      VARCHAR(10)     -> tipoAcao ('bloquear' ou 'liberar')
 * dominio.nome_dominio / dispositivo.nome_dispositivo (via JOIN) -> nomeDominio / nomeDispositivo
 * </pre>
 */
public record RegraPersonalizada(Integer id, int idDominio, String nomeDominio,
                                 String mac, String nomeDispositivo, String motivo,
                                 LocalDateTime dataCriacao, String tipoAcao) {

    /** Valores aceitos pelo CHECK ck_regra_tipo_acao. */
    public static final List<String> TIPOS_ACAO = List.of("bloquear", "liberar");
}
