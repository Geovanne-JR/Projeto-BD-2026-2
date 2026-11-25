package br.unb.bd.pihole.ui;

import br.unb.bd.pihole.modelo.RedeLocal;
import br.unb.bd.pihole.modelo.ServidorDns;
import br.unb.bd.pihole.modelo.Usuario;
import br.unb.bd.pihole.persistencia.RedeLocalDAO;
import br.unb.bd.pihole.persistencia.ServidorDnsDAO;
import br.unb.bd.pihole.persistencia.UsuarioDAO;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.Objects;

/**
 * Cadastro de redes locais, com os servidores DNS usados por cada rede
 * (tabela associativa usam).
 */
public class RedeLocalView extends CadastroView<RedeLocal> {

    private final RedeLocalDAO dao = new RedeLocalDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final ServidorDnsDAO servidorDAO = new ServidorDnsDAO();

    private final TextField campoSsid = new TextField();
    private final TextField campoFaixa = new TextField();
    private final TextField campoGateway = new TextField();
    private final ComboBox<Usuario> comboDono = new ComboBox<>();
    private final SelecaoMultipla<ServidorDns> servidores =
            new SelecaoMultipla<>("Servidores DNS usados pela rede:", ServidorDns::id);

    public RedeLocalView() {
        super("Rede local");
        coluna("ID", RedeLocal::id);
        coluna("SSID", RedeLocal::nomeSsid);
        coluna("Faixa de IP", RedeLocal::faixaIp);
        coluna("Gateway", RedeLocal::gateway);
        coluna("Responsável", RedeLocal::nomeUsuario);

        campoFaixa.setPromptText("ex.: 192.168.0.0/24");
        campoGateway.setPromptText("ex.: 192.168.0.1");
        campo("SSID:", campoSsid);
        campo("Faixa de IP:", campoFaixa);
        campo("Gateway:", campoGateway);
        campo("Responsável:", comboDono);
        extra(servidores);
    }

    @Override
    protected void carregarOpcoes() {
        opcoes(comboDono, usuarioDAO.listarTodos());
        servidores.setItens(servidorDAO.listarTodos());
    }

    @Override
    protected List<RedeLocal> listar() {
        return dao.listarTodos();
    }

    @Override
    protected void preencherFormulario(RedeLocal r) {
        campoSsid.setText(r.nomeSsid());
        campoFaixa.setText(r.faixaIp());
        campoGateway.setText(r.gateway());
        selecionar(comboDono, u -> Objects.equals(u.getIdUsuario(), r.idUsuario()));
        servidores.marcar(dao.listarServidores(r.id()));
    }

    @Override
    protected void limparCampos() {
        campoSsid.clear();
        campoFaixa.clear();
        campoGateway.clear();
        comboDono.setValue(null);
        servidores.desmarcarTodos();
    }

    @Override
    protected void salvar(RedeLocal existente) {
        String ssid = obrigatorio(campoSsid, "SSID");
        String faixa = obrigatorio(campoFaixa, "Faixa de IP");
        String gateway = obrigatorio(campoGateway, "Gateway");
        Usuario dono = obrigatorio(comboDono, "Responsável");

        int idRede;
        if (existente == null) {
            idRede = dao.inserir(new RedeLocal(null, dono.getIdUsuario(), dono.getNome(), ssid, faixa, gateway)).id();
        } else {
            idRede = existente.id();
            dao.atualizar(new RedeLocal(idRede, dono.getIdUsuario(), dono.getNome(), ssid, faixa, gateway));
        }
        dao.definirServidores(idRede, servidores.getIdsMarcados());
    }

    @Override
    protected void excluir(RedeLocal r) {
        dao.excluir(r.id());
    }

    @Override
    protected String descrever(RedeLocal r) {
        return "a rede " + r.nomeSsid();
    }
}
