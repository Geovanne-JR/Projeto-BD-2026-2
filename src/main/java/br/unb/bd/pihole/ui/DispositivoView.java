package br.unb.bd.pihole.ui;

import br.unb.bd.pihole.modelo.Dispositivo;
import br.unb.bd.pihole.modelo.ListaBloqueio;
import br.unb.bd.pihole.modelo.RedeLocal;
import br.unb.bd.pihole.modelo.Usuario;
import br.unb.bd.pihole.persistencia.DispositivoDAO;
import br.unb.bd.pihole.persistencia.ListaBloqueioDAO;
import br.unb.bd.pihole.persistencia.RedeLocalDAO;
import br.unb.bd.pihole.persistencia.UsuarioDAO;
import br.unb.bd.pihole.util.Formato;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.Objects;

/**
 * Cadastro de dispositivos, com as listas de bloqueio aplicadas a cada um
 * (tabela associativa tem). Envolve 5 tabelas: dispositivo, usuarios,
 * rede_local, lista_bloqueio e tem.
 *
 * O MAC é a chave primária: é digitado na inserção e fica travado na
 * edição.
 */
public class DispositivoView extends CadastroView<Dispositivo> {

    private static final List<String> TIPOS =
            List.of("celular", "notebook", "desktop", "tablet", "smart TV", "console", "IoT");

    private final DispositivoDAO dao = new DispositivoDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final RedeLocalDAO redeDAO = new RedeLocalDAO();
    private final ListaBloqueioDAO listaDAO = new ListaBloqueioDAO();

    private final TextField campoMac = new TextField();
    private final TextField campoNome = new TextField();
    private final ComboBox<String> comboTipo = new ComboBox<>();
    private final TextField campoIp = new TextField();
    private final TextField campoDns = new TextField();
    private final ComboBox<RedeLocal> comboRede = new ComboBox<>();
    private final ComboBox<Usuario> comboDono = new ComboBox<>();
    private final SelecaoMultipla<ListaBloqueio> listas =
            new SelecaoMultipla<>("Listas de bloqueio aplicadas:", ListaBloqueio::id);

    public DispositivoView() {
        super("Dispositivo");
        coluna("MAC", Dispositivo::mac);
        coluna("Nome", Dispositivo::nome);
        coluna("Tipo", d -> Formato.texto(d.tipo()));
        coluna("IP", d -> Formato.texto(d.ip()));
        coluna("Rede", Dispositivo::nomeRede);
        coluna("Dono", d -> d.nomeUsuario() == null ? "(sem dono)" : d.nomeUsuario());

        campoMac.setPromptText("ex.: aa:bb:cc:dd:ee:ff");
        campoIp.setPromptText("opcional");
        campoDns.setPromptText("opcional");
        comboTipo.setEditable(true);
        comboTipo.getItems().setAll(TIPOS);
        comboDono.setPromptText("(sem dono)");

        campo("MAC:", campoMac);
        campo("Nome:", campoNome);
        campo("Tipo:", comboTipo);
        campo("IP:", campoIp);
        campo("DNS configurado:", campoDns);
        campo("Rede:", comboRede);
        campo("Dono:", comBotaoLimpar(comboDono, "Sem dono"));
        extra(listas);
    }

    @Override
    protected void carregarOpcoes() {
        opcoes(comboRede, redeDAO.listarTodos());
        opcoes(comboDono, usuarioDAO.listarTodos());
        listas.setItens(listaDAO.listarTodos());
    }

    @Override
    protected List<Dispositivo> listar() {
        return dao.listarTodos();
    }

    @Override
    protected void preencherFormulario(Dispositivo d) {
        campoMac.setText(d.mac());
        campoMac.setDisable(true);
        campoNome.setText(d.nome());
        comboTipo.setValue(d.tipo());
        campoIp.setText(Formato.texto(d.ip()));
        campoDns.setText(Formato.texto(d.dns()));
        selecionar(comboRede, r -> r.id() == d.idRede());
        selecionar(comboDono, u -> Objects.equals(u.getIdUsuario(), d.idUsuario()));
        listas.marcar(dao.listarListas(d.mac()));
    }

    @Override
    protected void limparCampos() {
        campoMac.clear();
        campoMac.setDisable(false);
        campoNome.clear();
        comboTipo.setValue(null);
        comboTipo.getEditor().clear();
        campoIp.clear();
        campoDns.clear();
        comboRede.setValue(null);
        comboDono.setValue(null);
        listas.desmarcarTodos();
    }

    @Override
    protected void salvar(Dispositivo existente) {
        String mac = existente == null ? obrigatorio(campoMac, "MAC") : existente.mac();
        String nome = obrigatorio(campoNome, "Nome");
        RedeLocal rede = obrigatorio(comboRede, "Rede");
        Usuario dono = comboDono.getValue();
        // ComboBox editável: o texto digitado só vira getValue() após ENTER,
        // então lemos direto do campo de texto.
        String tipo = opcional(comboTipo.getEditor());

        Dispositivo d = new Dispositivo(mac,
                dono == null ? null : dono.getIdUsuario(),
                dono == null ? null : dono.getNome(),
                rede.id(), rede.nomeSsid(), nome,
                opcional(campoIp), tipo, opcional(campoDns));

        if (existente == null) {
            dao.inserir(d);
        } else {
            dao.atualizar(d);
        }
        dao.definirListas(mac, listas.getIdsMarcados());
    }

    @Override
    protected void excluir(Dispositivo d) {
        dao.excluir(d.mac());
    }

    @Override
    protected String descrever(Dispositivo d) {
        return "o dispositivo " + d.nome();
    }
}
