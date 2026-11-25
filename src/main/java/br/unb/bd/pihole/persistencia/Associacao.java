package br.unb.bd.pihole.persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Operações das tabelas associativas (usam, contem, tem), usadas pelos
 * DAOs de rede, lista e dispositivo. Os nomes de tabela e coluna vêm
 * sempre de constantes do código, nunca do usuário.
 *
 * Cada associação tem um "dono" (ex.: o dispositivo) e itens ligados a
 * ele (ex.: as listas de bloqueio, identificadas por INT).
 */
final class Associacao {

    private final String tabela;
    private final String colunaDono;
    private final String castDono;     // ex.: "::macaddr" quando o dono é um MAC
    private final String colunaItem;

    Associacao(String tabela, String colunaDono, String castDono, String colunaItem) {
        this.tabela = tabela;
        this.colunaDono = colunaDono;
        this.castDono = castDono;
        this.colunaItem = colunaItem;
    }

    /** IDs dos itens ligados ao dono. */
    Set<Integer> listar(Object dono) {
        String sql = "SELECT " + colunaItem + " FROM " + tabela
                + " WHERE " + colunaDono + " = ?" + castDono;
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, dono);
            try (ResultSet rs = ps.executeQuery()) {
                Set<Integer> ids = new HashSet<>();
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                }
                return ids;
            }
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    /**
     * Deixa o dono ligado exatamente aos itens informados, numa única
     * transação: remove os que saíram e insere os que entraram. Se algo
     * falhar no meio, o ROLLBACK desfaz tudo.
     */
    void definir(Object dono, Collection<Integer> itens) {
        String remover = "DELETE FROM " + tabela
                + " WHERE " + colunaDono + " = ?" + castDono
                + " AND NOT (" + colunaItem + " = ANY (?))";
        String inserir = "INSERT INTO " + tabela + " (" + colunaDono + ", " + colunaItem + ")"
                + " VALUES (?" + castDono + ", ?) ON CONFLICT DO NOTHING";

        try (Connection c = ConexaoBD.getConexao()) {
            c.setAutoCommit(false);
            try (PreparedStatement del = c.prepareStatement(remover);
                 PreparedStatement ins = c.prepareStatement(inserir)) {
                del.setObject(1, dono);
                del.setArray(2, c.createArrayOf("integer", itens.toArray()));
                del.executeUpdate();

                for (Integer item : itens) {
                    ins.setObject(1, dono);
                    ins.setInt(2, item);
                    ins.addBatch();
                }
                ins.executeBatch();
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }
}
