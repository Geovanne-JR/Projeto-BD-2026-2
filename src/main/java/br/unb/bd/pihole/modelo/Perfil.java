package br.unb.bd.pihole.modelo;

/** Linha da tabela perfis. {@code id} é null antes de inserir. */
public record Perfil(Integer id, String nome, String descricao) {

    /** Texto exibido no ComboBox. */
    @Override
    public String toString() {
        return nome;
    }
}
