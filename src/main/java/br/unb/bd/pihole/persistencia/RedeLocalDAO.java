package br.unb.bd.pihole.persistencia;

import br.unb.bd.pihole.modelo.RedeLocal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * CRUD da tabela rede_local e da associação usam (rede x servidor DNS).
 *
 * As colunas CIDR e INET são enviadas como texto e convertidas no SQL
 * ("?::cidr", "?::inet"). Se o texto não for um endereço válido, o banco
 * recusa com o erro 22P02.
 */
public class RedeLocalDAO implements DAO<RedeLocal, Integer> {

    private static final String SELECT =
            "SELECT r.id_rede, r.idusuarios, u.nome AS nome_usuario, r.nome_ssid, "
            + "r.faixa_ip, r.gateway "
            + "FROM rede_local r JOIN usuarios u ON u.id_usuario = r.idusuarios ";

    private final Associacao servidores = new Associacao("usam", "id_rede", "", "id_servidor");

    @Override
    public RedeLocal inserir(RedeLocal r) {
        String sql = "INSERT INTO rede_local (idusuarios, nome_ssid, faixa_ip, gateway) "
                + "VALUES (?, ?, ?::cidr, ?::inet) RETURNING id_rede";
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, r.idUsuario());
            ps.setString(2, r.nomeSsid());
            ps.setString(3, r.faixaIp());
            ps.setString(4, r.gateway());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new RedeLocal(rs.getInt(1), r.idUsuario(), r.nomeUsuario(),
                        r.nomeSsid(), r.faixaIp(), r.gateway());
            }
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    @Override
    public Optional<RedeLocal> buscarPorId(Integer id) {
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(SELECT + "WHERE r.id_rede = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    @Override
    public List<RedeLocal> listarTodos() {
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(SELECT + "ORDER BY r.nome_ssid");
             ResultSet rs = ps.executeQuery()) {
            List<RedeLocal> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    @Override
    public void atualizar(RedeLocal r) {
        String sql = "UPDATE rede_local SET idusuarios = ?, nome_ssid = ?, "
                + "faixa_ip = ?::cidr, gateway = ?::inet WHERE id_rede = ?";
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, r.idUsuario());
            ps.setString(2, r.nomeSsid());
            ps.setString(3, r.faixaIp());
            ps.setString(4, r.gateway());
            ps.setInt(5, r.id());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    /** Falha com erro 23503 se ainda houver dispositivos nesta rede. */
    @Override
    public void excluir(Integer id) {
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement("DELETE FROM rede_local WHERE id_rede = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    /** IDs dos servidores DNS usados pela rede (tabela usam). */
    public Set<Integer> listarServidores(int idRede) {
        return servidores.listar(idRede);
    }

    /** Define quais servidores DNS a rede usa (tabela usam). */
    public void definirServidores(int idRede, Collection<Integer> idsServidores) {
        servidores.definir(idRede, idsServidores);
    }

    private static RedeLocal mapear(ResultSet rs) throws SQLException {
        return new RedeLocal(
                rs.getInt("id_rede"),
                rs.getInt("idusuarios"),
                rs.getString("nome_usuario"),
                rs.getString("nome_ssid"),
                rs.getString("faixa_ip"),
                rs.getString("gateway"));
    }
}
