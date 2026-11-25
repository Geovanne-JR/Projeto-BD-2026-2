package br.unb.bd.pihole.persistencia;

import br.unb.bd.pihole.modelo.Dispositivo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * CRUD da tabela dispositivo e da associação tem (dispositivo x lista de
 * bloqueio).
 *
 * A chave primária é o MAC, informado pelo usuário (não há IDENTITY). Por
 * ser MACADDR, ele vai como texto com "?::macaddr".
 */
public class DispositivoDAO implements DAO<Dispositivo, String> {

    // LEFT JOIN em usuarios porque o dispositivo pode não ter dono.
    private static final String SELECT =
            "SELECT d.mac_address, d.idusuarios, u.nome AS nome_usuario, d.idrede_local, "
            + "r.nome_ssid, d.nome_dispositivo, d.ip_aparelho, d.tipo_dispositivo, d.dns "
            + "FROM dispositivo d "
            + "LEFT JOIN usuarios u ON u.id_usuario = d.idusuarios "
            + "JOIN rede_local r ON r.id_rede = d.idrede_local ";

    private final Associacao listas = new Associacao("tem", "mac_address", "::macaddr", "id_lista");

    @Override
    public Dispositivo inserir(Dispositivo d) {
        String sql = "INSERT INTO dispositivo (mac_address, idusuarios, idrede_local, nome_dispositivo, "
                + "ip_aparelho, tipo_dispositivo, dns) "
                + "VALUES (?::macaddr, ?, ?, ?, ?::inet, ?, ?::inet)";
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            preencher(ps, d);
            ps.executeUpdate();
            return d;
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    @Override
    public Optional<Dispositivo> buscarPorId(String mac) {
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(SELECT + "WHERE d.mac_address = ?::macaddr")) {
            ps.setString(1, mac);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    @Override
    public List<Dispositivo> listarTodos() {
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(SELECT + "ORDER BY d.nome_dispositivo");
             ResultSet rs = ps.executeQuery()) {
            List<Dispositivo> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    /** Atualiza tudo, menos o MAC (é a chave e identifica a linha). */
    @Override
    public void atualizar(Dispositivo d) {
        String sql = "UPDATE dispositivo SET idusuarios = ?, idrede_local = ?, nome_dispositivo = ?, "
                + "ip_aparelho = ?::inet, tipo_dispositivo = ?, dns = ?::inet "
                + "WHERE mac_address = ?::macaddr";
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (d.idUsuario() == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, d.idUsuario());
            }
            ps.setInt(2, d.idRede());
            ps.setString(3, d.nome());
            ps.setString(4, d.ip());
            ps.setString(5, d.tipo());
            ps.setString(6, d.dns());
            ps.setString(7, d.mac());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    /**
     * Remove também as regras e os vínculos em tem (ON DELETE CASCADE).
     * Falha com erro 23503 se o dispositivo tiver requisições no histórico.
     */
    @Override
    public void excluir(String mac) {
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement("DELETE FROM dispositivo WHERE mac_address = ?::macaddr")) {
            ps.setString(1, mac);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    /** IDs das listas de bloqueio aplicadas ao dispositivo (tabela tem). */
    public Set<Integer> listarListas(String mac) {
        return listas.listar(mac);
    }

    /** Define quais listas de bloqueio valem para o dispositivo (tabela tem). */
    public void definirListas(String mac, Collection<Integer> idsListas) {
        listas.definir(mac, idsListas);
    }

    private static void preencher(PreparedStatement ps, Dispositivo d) throws SQLException {
        ps.setString(1, d.mac());
        if (d.idUsuario() == null) {
            ps.setNull(2, Types.INTEGER);
        } else {
            ps.setInt(2, d.idUsuario());
        }
        ps.setInt(3, d.idRede());
        ps.setString(4, d.nome());
        ps.setString(5, d.ip());
        ps.setString(6, d.tipo());
        ps.setString(7, d.dns());
    }

    private static Dispositivo mapear(ResultSet rs) throws SQLException {
        return new Dispositivo(
                rs.getString("mac_address"),
                rs.getObject("idusuarios", Integer.class),   // null = sem dono
                rs.getString("nome_usuario"),
                rs.getInt("idrede_local"),
                rs.getString("nome_ssid"),
                rs.getString("nome_dispositivo"),
                rs.getString("ip_aparelho"),
                rs.getString("tipo_dispositivo"),
                rs.getString("dns"));
    }
}
