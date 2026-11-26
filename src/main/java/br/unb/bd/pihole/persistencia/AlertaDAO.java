package br.unb.bd.pihole.persistencia;

import br.unb.bd.pihole.modelo.Alerta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Alertas de segurança. Quem cria os alertas é o trigger
 * trg_alerta_requisicao; a aplicação só lista, marca como visto e exclui.
 */
public class AlertaDAO {

    public List<Alerta> listarTodos() {
        String sql = "SELECT a.id_alerta, a.idrequisicoes, a.visualizado, a.mensagem, a.gravidade, "
                + "a.data_geracao, d.nome_dispositivo, dm.nome_dominio "
                + "FROM alertas_seguranca a "
                + "JOIN requisicoes r ON r.id_requisicao = a.idrequisicoes "
                + "JOIN dispositivo d ON d.mac_address = r.iddispositivo "
                + "JOIN dominio dm ON dm.id_dominio = r.iddominio "
                + "ORDER BY a.visualizado, a.data_geracao DESC";
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Alerta> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(new Alerta(
                        rs.getInt("id_alerta"),
                        rs.getInt("idrequisicoes"),
                        rs.getBoolean("visualizado"),
                        rs.getString("mensagem"),
                        rs.getString("gravidade"),
                        rs.getObject("data_geracao", LocalDateTime.class),
                        rs.getString("nome_dispositivo"),
                        rs.getString("nome_dominio")));
            }
            return lista;
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    public void marcarVisualizado(int idAlerta, boolean visualizado) {
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE alertas_seguranca SET visualizado = ? WHERE id_alerta = ?")) {
            ps.setBoolean(1, visualizado);
            ps.setInt(2, idAlerta);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    public void excluir(int idAlerta) {
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement("DELETE FROM alertas_seguranca WHERE id_alerta = ?")) {
            ps.setInt(1, idAlerta);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }
}
