package br.unb.bd.pihole.persistencia;

import br.unb.bd.pihole.modelo.ServidorDns;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** CRUD da tabela servidor_dns. */
public class ServidorDnsDAO implements DAO<ServidorDns, Integer> {

    private static final String SELECT =
            "SELECT id_servidor, nome_servidor, ip_primario, ip_secundario, suporta_doh FROM servidor_dns ";

    @Override
    public ServidorDns inserir(ServidorDns s) {
        String sql = "INSERT INTO servidor_dns (nome_servidor, ip_primario, ip_secundario, suporta_doh) "
                + "VALUES (?, ?::inet, ?::inet, ?) RETURNING id_servidor";
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.nome());
            ps.setString(2, s.ipPrimario());
            ps.setString(3, s.ipSecundario());
            ps.setBoolean(4, s.suportaDoh());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new ServidorDns(rs.getInt(1), s.nome(), s.ipPrimario(), s.ipSecundario(), s.suportaDoh());
            }
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    @Override
    public Optional<ServidorDns> buscarPorId(Integer id) {
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(SELECT + "WHERE id_servidor = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    @Override
    public List<ServidorDns> listarTodos() {
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(SELECT + "ORDER BY nome_servidor");
             ResultSet rs = ps.executeQuery()) {
            List<ServidorDns> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    @Override
    public void atualizar(ServidorDns s) {
        String sql = "UPDATE servidor_dns SET nome_servidor = ?, ip_primario = ?::inet, "
                + "ip_secundario = ?::inet, suporta_doh = ? WHERE id_servidor = ?";
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.nome());
            ps.setString(2, s.ipPrimario());
            ps.setString(3, s.ipSecundario());
            ps.setBoolean(4, s.suportaDoh());
            ps.setInt(5, s.id());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    /** Remove também os vínculos em usam (ON DELETE CASCADE). */
    @Override
    public void excluir(Integer id) {
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement("DELETE FROM servidor_dns WHERE id_servidor = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    private static ServidorDns mapear(ResultSet rs) throws SQLException {
        return new ServidorDns(
                rs.getInt("id_servidor"),
                rs.getString("nome_servidor"),
                rs.getString("ip_primario"),
                rs.getString("ip_secundario"),
                rs.getBoolean("suporta_doh"));
    }
}
