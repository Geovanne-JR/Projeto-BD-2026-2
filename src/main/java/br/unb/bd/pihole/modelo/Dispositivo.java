package br.unb.bd.pihole.modelo;

/**
 * Linha da tabela dispositivo. A chave primária é o MAC (não é gerada
 * pelo banco: o usuário informa).
 *
 * <pre>
 * mac_address       MACADDR (PK)    -> mac  (texto, ex.: "aa:bb:cc:dd:ee:ff")
 * idusuarios        INT (FK, null)  -> idUsuario (null = sem dono)
 * idrede_local      INT (FK)        -> idRede
 * nome_dispositivo  VARCHAR(100)    -> nome
 * ip_aparelho       INET (null)     -> ip
 * tipo_dispositivo  VARCHAR(50)     -> tipo
 * dns               INET (null)     -> dns
 * usuarios.nome / rede_local.nome_ssid (via JOIN) -> nomeUsuario / nomeRede
 * </pre>
 */
public record Dispositivo(String mac, Integer idUsuario, String nomeUsuario,
                          int idRede, String nomeRede, String nome,
                          String ip, String tipo, String dns) {

    @Override
    public String toString() {
        return nome + " (" + mac + ")";
    }
}
