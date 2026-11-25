package br.unb.bd.pihole.modelo;

import java.util.List;

/**
 * Linha da tabela cat_dominio.
 *
 * <pre>
 * id_categoria    INT (identity)    -> id
 * nome_categoria  VARCHAR(50)       -> nome
 * nivel_risco     nivel_risco_enum  -> nivelRisco (texto: baixo/medio/alto/critico)
 * </pre>
 */
public record CategoriaDominio(Integer id, String nome, String nivelRisco) {

    /** Valores aceitos pelo tipo nivel_risco_enum do banco. */
    public static final List<String> NIVEIS_RISCO = List.of("baixo", "medio", "alto", "critico");

    @Override
    public String toString() {
        return nome;
    }
}
