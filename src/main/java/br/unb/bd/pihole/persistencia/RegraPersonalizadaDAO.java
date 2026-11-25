package br.unb.bd.pihole.persistencia;

import br.unb.bd.pihole.modelo.RegraPersonalizada;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CRUD da tabela regra_personalizada. A constraint UNIQUE
 * (iddominio, iddispositivo) impede duas regras para o mesmo par; nesse
 * caso o banco devolve o erro 23505.
 */
public class RegraPersonalizadaDAO implements DAO<RegraPersonalizada, Integer> {

    private static final String SELECT =
            "SELECT r.id_regra, r.iddominio, dm.nome_dominio, r.iddispositivo, d.nome_dispositivo, "
            + "r.motivo, r.data_criacao, r.tipo_acao "
            + "FROM regra_personalizada r "
            + "JOIN dominio dm ON dm.id_dominio = r.iddominio "
            + "JOIN dispositivo d ON d.mac_address = r.iddispositivo ";

    @Override
    public RegraPersonalizada inserir(RegraPersonalizada r) {
        String sql = "INSERT INTO regra_personalizada (iddominio, iddispositivo, motivo, tipo_acao) "
                + "VALUES (?, ?::macaddr, ?, ?) RETURNING id_regra, data_criacao";
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, r.idDominio());
            ps.setString(2, r.mac());
            ps.setString(3, r.motivo());
            ps.setString(4, r.tipoAcao());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new RegraPersonalizada(rs.getInt(1), r.idDominio(), r.nomeDominio(), r.mac(),
                        r.nomeDispositivo(), r.motivo(), rs.getObject(2, LocalDateTime.class), r.tipoAcao());
            }
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    @Override
    public Optional<RegraPersonalizada> buscarPorId(Integer id) {
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(SELECT + "WHERE r.id_regra = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    @Override
    public List<RegraPersonalizada> listarTodos() {
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(SELECT + "ORDER BY d.nome_dispositivo, dm.nome_dominio");
             ResultSet rs = ps.executeQuery()) {
            List<RegraPersonalizada> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    @Override
    public void atualizar(RegraPersonalizada r) {
        String sql = "UPDATE regra_personalizada SET iddominio = ?, iddispositivo = ?::macaddr, "
                + "motivo = ?, tipo_acao = ? WHERE id_regra = ?";
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, r.idDominio());
            ps.setString(2, r.mac());
            ps.setString(3, r.motivo());
            ps.setString(4, r.tipoAcao());
            ps.setInt(5, r.id());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    @Override
    public void excluir(Integer id) {
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement("DELETE FROM regra_personalizada WHERE id_regra = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    private static RegraPersonalizada mapear(ResultSet rs) throws SQLException {
        return new RegraPersonalizada(
                rs.getInt("id_regra"),
                rs.getInt("iddominio"),
                rs.getString("nome_dominio"),
                rs.getString("iddispositivo"),
                rs.getString("nome_dispositivo"),
                rs.getString("motivo"),
                rs.getObject("data_criacao", LocalDateTime.class),
                rs.getString("tipo_acao"));
    }
}
