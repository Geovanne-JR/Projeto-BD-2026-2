package br.unb.bd.pihole.modelo;

/**
 * Linha da tabela dominio.
 *
 * <pre>
 * id_dominio     INT (identity)  -> id
 * idcat_dominio  INT (FK, null)  -> idCategoria (null = sem categoria)
 * nome_dominio   VARCHAR(253)    -> nome
 * cat_dominio.nome_categoria (via JOIN) -> nomeCategoria (só leitura)
 * </pre>
 */
public record Dominio(Integer id, Integer idCategoria, String nomeCategoria, String nome) {

    @Override
    public String toString() {
        return nome;
    }
}
