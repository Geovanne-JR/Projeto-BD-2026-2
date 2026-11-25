package br.unb.bd.pihole.persistencia;

import br.unb.bd.pihole.modelo.RegistroRequisicao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Histórico de requisições DNS. Não é um CRUD: as requisições são geradas
 * pelo tráfego da rede, então só existem leitura (pela VIEW) e registro
 * (pela PROCEDURE).
 */
public class RequisicaoDAO {

    /** Lê o histórico pela view vw_historico_requisicoes, mais recente primeiro. */
    public List<RegistroRequisicao> listarHistorico(int limite) {
        String sql = "SELECT * FROM vw_historico_requisicoes ORDER BY data_hora DESC LIMIT ?";
        try (Connection c = ConexaoBD.getConexao();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                List<RegistroRequisicao> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(new RegistroRequisicao(
                            rs.getInt("id_requisicao"),
                            rs.getObject("data_hora", LocalDateTime.class),
                            rs.getString("mac_address"),
                            rs.getString("nome_dispositivo"),
                            rs.getString("dono"),
                            rs.getString("nome_dominio"),
                            rs.getString("nome_categoria"),
                            rs.getString("nivel_risco"),
                            rs.getString("status"),
                            rs.getObject("tempo_resposta_ms", Integer.class)));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }

    /**
     * Registra uma requisição chamando a procedure registrar_requisicao, que
     * decide o status. Se ela for bloqueada e o domínio for de risco alto, o
     * trigger trg_alerta_requisicao cria o alerta.
     *
     * @return o status que o banco decidiu (permitida, bloqueada ou cache)
     */
    public String registrar(String mac, int idDominio, Integer tempoMs) {
        String consultarStatus = "SELECT status FROM requisicoes "
                + "WHERE iddispositivo = ?::macaddr AND iddominio = ? "
                + "ORDER BY id_requisicao DESC LIMIT 1";
        try (Connection c = ConexaoBD.getConexao()) {
            try (PreparedStatement call = c.prepareStatement("CALL registrar_requisicao(?::macaddr, ?, ?)")) {
                call.setString(1, mac);
                call.setInt(2, idDominio);
                if (tempoMs == null) {
                    call.setNull(3, Types.INTEGER);
                } else {
                    call.setInt(3, tempoMs);
                }
                call.execute();
            }
            try (PreparedStatement ps = c.prepareStatement(consultarStatus)) {
                ps.setString(1, mac);
                ps.setInt(2, idDominio);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getString(1) : "?";
                }
            }
        } catch (SQLException e) {
            throw PersistenciaException.de(e);
        }
    }
}
