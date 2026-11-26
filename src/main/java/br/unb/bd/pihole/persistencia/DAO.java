package br.unb.bd.pihole.persistencia;

import java.util.List;
import java.util.Optional;

/**
 * Contrato do CRUD de uma tabela.
 *
 * @param <T> classe do modelo (ex.: Usuario)
 * @param <K> tipo da chave primária (Integer, ou String para o MAC)
 */
public interface DAO<T, K> {

    /** Insere e devolve o objeto com a chave gerada pelo banco preenchida. */
    T inserir(T obj);

    Optional<T> buscarPorId(K id);

    List<T> listarTodos();

    void atualizar(T obj);

    void excluir(K id);
}
