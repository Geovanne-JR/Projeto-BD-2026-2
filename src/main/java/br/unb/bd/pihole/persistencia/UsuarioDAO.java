package br.unb.bd.pihole.persistencia;

import br.unb.bd.pihole.modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CRUD da tabela usuarios. É o modelo para os outros DAOs.
 *
 * Pontos que valem para todos:
 * - sempre PreparedStatement com "?" (nunca concatenar texto no SQL);
 * - try-with-resources fecha Connection/Statement/ResultSet sozinho;
 * - SQLException vira PersistenciaException com mensagem amigável;
 * - a chave identity vem do banco via RETURNING.
 */
public class UsuarioDAO implements DAO<Usuario, Integer> {

    // A listagem não traz a foto: BYTEA pode ser grande e a tabela não mostra.
    // O JOIN com perfis traz o nome do perfil (duas tabelas relacionadas).
    private static final String SELECT_LISTA =
            "SELECT u.id_usuario, u.idperfis, u.nome, u.email, u.senha_hash, p.nomeperfil "
            + "FROM usuarios u JOIN perfis p ON p.id_perfil = u.idperfis ";

    @Override
    public Usuario inserir(Usuario u) {
        String sql = "INSERT INTO usuarios (idperfis, nome, email, foto_perfil, senha_hash) "
                + "VALUES (?, ?, ?, ?, ?) RETURNING id_usuario";
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, u.getIdPerfil());
            ps.setString(2, u.getNome());
            ps.setString(3, u.getEmail());
            ps.setBytes(4, u.getFotoPerfil());   // null grava NULL
            ps.setString(5, u.getSenhaHash());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                u.setIdUsuario(rs.getInt(1));
            }
            return u;
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    /** Busca um usuário completo, incluindo a foto. */
    @Override
    public Optional<Usuario> buscarPorId(Integer id) {
        String sql = "SELECT u.id_usuario, u.idperfis, u.nome, u.email, u.senha_hash, "
                + "u.foto_perfil, p.nomeperfil "
                + "FROM usuarios u JOIN perfis p ON p.id_perfil = u.idperfis "
                + "WHERE u.id_usuario = ?";
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Usuario u = mapear(rs);
                u.setFotoPerfil(rs.getBytes("foto_perfil"));
                return Optional.of(u);
            }
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    @Override
    public List<Usuario> listarTodos() {
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(SELECT_LISTA + "ORDER BY u.nome");
             ResultSet rs = ps.executeQuery()) {
            List<Usuario> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    /** Atualiza perfil, nome, email e senha. A foto tem método próprio. */
    @Override
    public void atualizar(Usuario u) {
        String sql = "UPDATE usuarios SET idperfis = ?, nome = ?, email = ?, senha_hash = ? "
                + "WHERE id_usuario = ?";
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, u.getIdPerfil());
            ps.setString(2, u.getNome());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getSenhaHash());
            ps.setInt(5, u.getIdUsuario());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    /** Grava (ou remove, se {@code foto} for null) a foto de perfil. */
    public void atualizarFoto(int idUsuario, byte[] foto) {
        String sql = "UPDATE usuarios SET foto_perfil = ? WHERE id_usuario = ?";
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBytes(1, foto);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    /**
     * Falha com erro 23503 se o usuário ainda for dono de uma rede local
     * (rede_local.idusuarios não tem ON DELETE). Os dispositivos dele ficam
     * sem dono (ON DELETE SET NULL).
     */
    @Override
    public void excluir(Integer id) {
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement("DELETE FROM usuarios WHERE id_usuario = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    private static Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setIdPerfil(rs.getInt("idperfis"));
        u.setNome(rs.getString("nome"));
        u.setEmail(rs.getString("email"));
        u.setSenhaHash(rs.getString("senha_hash"));
        u.setNomePerfil(rs.getString("nomeperfil"));
        return u;
    }
}
