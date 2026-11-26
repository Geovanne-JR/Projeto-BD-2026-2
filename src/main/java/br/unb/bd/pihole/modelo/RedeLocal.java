package br.unb.bd.pihole.modelo;

/**
 * Linha da tabela rede_local.
 *
 * <pre>
 * id_rede     INT (identity) -> id
 * idusuarios  INT (FK)       -> idUsuario
 * nome_ssid   VARCHAR(32)    -> nomeSsid
 * faixa_ip    CIDR           -> faixaIp  (texto, ex.: "192.168.0.0/24")
 * gateway     INET           -> gateway  (texto, ex.: "192.168.0.1")
 * usuarios.nome (via JOIN)   -> nomeUsuario (só leitura)
 * </pre>
 */
public record RedeLocal(Integer id, int idUsuario, String nomeUsuario,
                        String nomeSsid, String faixaIp, String gateway) {

    @Override
    public String toString() {
        return nomeSsid;
    }
}
