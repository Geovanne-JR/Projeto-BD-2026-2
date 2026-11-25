package br.unb.bd.pihole.persistencia;

import br.unb.bd.pihole.modelo.Dominio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** CRUD da tabela dominio. */
public class DominioDAO implements DAO<Dominio, Integer> {

    // LEFT JOIN porque o domínio pode estar sem categoria.
    private static final String SELECT =
            "SELECT d.id_dominio, d.idcat_dominio, c.nome_categoria, d.nome_dominio "
            + "FROM dominio d LEFT JOIN cat_dominio c ON c.id_categoria = d.idcat_dominio ";

    @Override
    public Dominio inserir(Dominio d) {
        String sql = "INSERT INTO dominio (idcat_dominio, nome_dominio) VALUES (?, ?) RETURNING id_dominio";
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            definirCategoria(ps, 1, d.idCategoria());
            ps.setString(2, d.nome());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new Dominio(rs.getInt(1), d.idCategoria(), d.nomeCategoria(), d.nome());
            }
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    @Override
    public Optional<Dominio> buscarPorId(Integer id) {
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(SELECT + "WHERE d.id_dominio = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    @Override
    public List<Dominio> listarTodos() {
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(SELECT + "ORDER BY d.nome_dominio");
             ResultSet rs = ps.executeQuery()) {
            List<Dominio> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    @Override
    public void atualizar(Dominio d) {
        String sql = "UPDATE dominio SET idcat_dominio = ?, nome_dominio = ? WHERE id_dominio = ?";
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            definirCategoria(ps, 1, d.idCategoria());
            ps.setString(2, d.nome());
            ps.setInt(3, d.id());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    /**
     * Remove também as regras e os vínculos em contem (ON DELETE CASCADE).
     * Falha com erro 23503 se o domínio aparecer no histórico de requisições.
     */
    @Override
    public void excluir(Integer id) {
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement("DELETE FROM dominio WHERE id_dominio = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    private static void definirCategoria(PreparedStatement ps, int indice, Integer idCategoria) throws SQLException {
        if (idCategoria == null) {
            ps.setNull(indice, Types.INTEGER);
        } else {
            ps.setInt(indice, idCategoria);
        }
    }

    private static Dominio mapear(ResultSet rs) throws SQLException {
        return new Dominio(
                rs.getInt("id_dominio"),
                rs.getObject("idcat_dominio", Integer.class),   // null = sem categoria
                rs.getString("nome_categoria"),
                rs.getString("nome_dominio"));
    }
}
