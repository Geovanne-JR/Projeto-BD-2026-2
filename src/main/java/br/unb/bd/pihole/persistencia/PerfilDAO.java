package br.unb.bd.pihole.persistencia;

import br.unb.bd.pihole.modelo.Perfil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** CRUD da tabela perfis. */
public class PerfilDAO implements DAO<Perfil, Integer> {

    @Override
    public Perfil inserir(Perfil p) {
        String sql = "INSERT INTO perfis (nomeperfil, descricao) VALUES (?, ?) RETURNING id_perfil";
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.nome());
            ps.setString(2, p.descricao());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new Perfil(rs.getInt(1), p.nome(), p.descricao());
            }
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    @Override
    public Optional<Perfil> buscarPorId(Integer id) {
        String sql = "SELECT id_perfil, nomeperfil, descricao FROM perfis WHERE id_perfil = ?";
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    @Override
    public List<Perfil> listarTodos() {
        String sql = "SELECT id_perfil, nomeperfil, descricao FROM perfis ORDER BY nomeperfil";
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Perfil> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    @Override
    public void atualizar(Perfil p) {
        String sql = "UPDATE perfis SET nomeperfil = ?, descricao = ? WHERE id_perfil = ?";
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.nome());
            ps.setString(2, p.descricao());
            ps.setInt(3, p.id());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    /** Falha com erro 23503 se algum usuário ainda usa o perfil. */
    @Override
    public void excluir(Integer id) {
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement("DELETE FROM perfis WHERE id_perfil = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    private static Perfil mapear(ResultSet rs) throws SQLException {
        return new Perfil(rs.getInt("id_perfil"), rs.getString("nomeperfil"), rs.getString("descricao"));
    }
}
