package br.unb.bd.pihole.persistencia;

import br.unb.bd.pihole.modelo.ListaBloqueio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * CRUD da tabela lista_bloqueio e da associação contem (lista x domínio).
 *
 * data_atualizacao é mantida pelo banco: DEFAULT na inserção e o trigger
 * trg_contem_atualiza_lista quando os domínios da lista mudam.
 */
public class ListaBloqueioDAO implements DAO<ListaBloqueio, Integer> {

    private static final String SELECT =
            "SELECT id_lista, nome_lista, data_atualizacao, ativa, url_origem FROM lista_bloqueio ";

    private final Associacao dominios = new Associacao("contem", "id_lista", "", "id_dominio");

    @Override
    public ListaBloqueio inserir(ListaBloqueio l) {
        String sql = "INSERT INTO lista_bloqueio (nome_lista, ativa, url_origem) VALUES (?, ?, ?) "
                + "RETURNING id_lista, data_atualizacao";
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, l.nome());
            ps.setBoolean(2, l.ativa());
            ps.setString(3, l.urlOrigem());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new ListaBloqueio(rs.getInt(1), l.nome(),
                        rs.getObject(2, LocalDateTime.class), l.ativa(), l.urlOrigem());
            }
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    @Override
    public Optional<ListaBloqueio> buscarPorId(Integer id) {
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(SELECT + "WHERE id_lista = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    @Override
    public List<ListaBloqueio> listarTodos() {
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(SELECT + "ORDER BY nome_lista");
             ResultSet rs = ps.executeQuery()) {
            List<ListaBloqueio> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    @Override
    public void atualizar(ListaBloqueio l) {
        String sql = "UPDATE lista_bloqueio SET nome_lista = ?, ativa = ?, url_origem = ?, "
                + "data_atualizacao = CURRENT_TIMESTAMP WHERE id_lista = ?";
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, l.nome());
            ps.setBoolean(2, l.ativa());
            ps.setString(3, l.urlOrigem());
            ps.setInt(4, l.id());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    /** Remove também os vínculos em contem e tem (ON DELETE CASCADE). */
    @Override
    public void excluir(Integer id) {
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement("DELETE FROM lista_bloqueio WHERE id_lista = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    /** IDs dos domínios da lista (tabela contem). */
    public Set<Integer> listarDominios(int idLista) {
        return dominios.listar(idLista);
    }

    /** Define os domínios da lista (tabela contem). */
    public void definirDominios(int idLista, Collection<Integer> idsDominios) {
        dominios.definir(idLista, idsDominios);
    }

    private static ListaBloqueio mapear(ResultSet rs) throws SQLException {
        return new ListaBloqueio(
                rs.getInt("id_lista"),
                rs.getString("nome_lista"),
                rs.getObject("data_atualizacao", LocalDateTime.class),
                rs.getBoolean("ativa"),
                rs.getString("url_origem"));
    }
}
