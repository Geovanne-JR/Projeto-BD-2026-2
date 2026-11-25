package br.unb.bd.pihole.persistencia;

import br.unb.bd.pihole.modelo.CategoriaDominio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CRUD da tabela cat_dominio. O nível de risco é um ENUM do PostgreSQL,
 * enviado como texto com "?::nivel_risco_enum".
 */
public class CategoriaDominioDAO implements DAO<CategoriaDominio, Integer> {

    private static final String SELECT =
            "SELECT id_categoria, nome_categoria, nivel_risco FROM cat_dominio ";

    @Override
    public CategoriaDominio inserir(CategoriaDominio cat) {
        String sql = "INSERT INTO cat_dominio (nome_categoria, nivel_risco) "
                + "VALUES (?, ?::nivel_risco_enum) RETURNING id_categoria";
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, cat.nome());
            ps.setString(2, cat.nivelRisco());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new CategoriaDominio(rs.getInt(1), cat.nome(), cat.nivelRisco());
            }
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    @Override
    public Optional<CategoriaDominio> buscarPorId(Integer id) {
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(SELECT + "WHERE id_categoria = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    @Override
    public List<CategoriaDominio> listarTodos() {
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(SELECT + "ORDER BY nome_categoria");
             ResultSet rs = ps.executeQuery()) {
            List<CategoriaDominio> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    @Override
    public void atualizar(CategoriaDominio cat) {
        String sql = "UPDATE cat_dominio SET nome_categoria = ?, nivel_risco = ?::nivel_risco_enum "
                + "WHERE id_categoria = ?";
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, cat.nome());
            ps.setString(2, cat.nivelRisco());
            ps.setInt(3, cat.id());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    /** Os domínios da categoria ficam sem categoria (ON DELETE SET NULL). */
    @Override
    public void excluir(Integer id) {
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement("DELETE FROM cat_dominio WHERE id_categoria = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    private static CategoriaDominio mapear(ResultSet rs) throws SQLException {
        return new CategoriaDominio(
                rs.getInt("id_categoria"),
                rs.getString("nome_categoria"),
                rs.getString("nivel_risco"));
    }
}
