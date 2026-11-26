package br.unb.bd.pihole.modelo;

/**
 * Linha da tabela servidor_dns.
 *
 * <pre>
 * id_servidor    INT (identity) -> id
 * nome_servidor  VARCHAR(100)   -> nome
 * ip_primario    INET           -> ipPrimario
 * ip_secundario  INET (null)    -> ipSecundario
 * suporta_doh    BOOLEAN        -> suportaDoh
 * </pre>
 */
public record ServidorDns(Integer id, String nome, String ipPrimario,
                          String ipSecundario, boolean suportaDoh) {

    @Override
    public String toString() {
        return nome + " (" + ipPrimario + ")";
    }
}
