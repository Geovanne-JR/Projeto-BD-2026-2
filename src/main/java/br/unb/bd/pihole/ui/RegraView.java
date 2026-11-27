package br.unb.bd.pihole.ui;

import br.unb.bd.pihole.modelo.Dispositivo;
import br.unb.bd.pihole.modelo.Dominio;
import br.unb.bd.pihole.modelo.RegraPersonalizada;
import br.unb.bd.pihole.persistencia.DispositivoDAO;
import br.unb.bd.pihole.persistencia.DominioDAO;
import br.unb.bd.pihole.persistencia.RegraPersonalizadaDAO;
import br.unb.bd.pihole.util.Formato;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.util.List;

/**
 * Cadastro de regras personalizadas: bloquear ou liberar um domínio em um
 * dispositivo específico. A regra tem prioridade sobre as listas.
 */
public class RegraView extends CadastroView<RegraPersonalizada> {

    private final RegraPersonalizadaDAO dao = new RegraPersonalizadaDAO();
    private final DispositivoDAO dispositivoDAO = new DispositivoDAO();
    private final DominioDAO dominioDAO = new DominioDAO();

    private final ComboBox<Dispositivo> comboDispositivo = new ComboBox<>();
    private final ComboBox<Dominio> comboDominio = new ComboBox<>();
    private final ComboBox<String> comboAcao = new ComboBox<>();
    private final TextField campoMotivo = new TextField();

    public RegraView() {
        super("Regra personalizada");
        coluna("ID", RegraPersonalizada::id);
        coluna("Dispositivo", RegraPersonalizada::nomeDispositivo);
        coluna("Domínio", RegraPersonalizada::nomeDominio);
        coluna("Ação", RegraPersonalizada::tipoAcao);
        coluna("Motivo", r -> Formato.texto(r.motivo()));
        coluna("Criada em", r -> Formato.dataHora(r.dataCriacao()));

        comboAcao.getItems().setAll(RegraPersonalizada.TIPOS_ACAO);
        campoMotivo.setPromptText("opcional");
        campo("Dispositivo:", comboDispositivo);
        campo("Domínio:", comboDominio);
        campo("Ação:", comboAcao);
        campo("Motivo:", campoMotivo);
    }

    @Override
    protected void carregarOpcoes() {
        opcoes(comboDispositivo, dispositivoDAO.listarTodos());
        opcoes(comboDominio, dominioDAO.listarTodos());
    }

    @Override
    protected List<RegraPersonalizada> listar() {
        return dao.listarTodos();
    }

    @Override
    protected void preencherFormulario(RegraPersonalizada r) {
        selecionar(comboDispositivo, d -> d.mac().equals(r.mac()));
        selecionar(comboDominio, d -> d.id() == r.idDominio());
        comboAcao.setValue(r.tipoAcao());
        campoMotivo.setText(Formato.texto(r.motivo()));
    }

    @Override
    protected void limparCampos() {
        comboDispositivo.setValue(null);
        comboDominio.setValue(null);
        comboAcao.setValue(null);
        campoMotivo.clear();
    }

    @Override
    protected void salvar(RegraPersonalizada existente) {
        Dispositivo disp = obrigatorio(comboDispositivo, "Dispositivo");
        Dominio dom = obrigatorio(comboDominio, "Domínio");
        String acao = obrigatorio(comboAcao, "Ação");
        String motivo = opcional(campoMotivo);

        Integer id = existente == null ? null : existente.id();
        RegraPersonalizada r = new RegraPersonalizada(id, dom.id(), dom.nome(), disp.mac(),
                disp.nome(), motivo, null, acao);
        if (existente == null) {
            dao.inserir(r);
        } else {
            dao.atualizar(r);
        }
    }

    @Override
    protected void excluir(RegraPersonalizada r) {
        dao.excluir(r.id());
    }

    @Override
    protected String descrever(RegraPersonalizada r) {
        return "a regra \"" + r.tipoAcao() + " " + r.nomeDominio() + "\" de " + r.nomeDispositivo();
    }
}
