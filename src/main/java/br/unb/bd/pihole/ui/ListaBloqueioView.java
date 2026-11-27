package br.unb.bd.pihole.ui;

import br.unb.bd.pihole.modelo.Dominio;
import br.unb.bd.pihole.modelo.ListaBloqueio;
import br.unb.bd.pihole.persistencia.DominioDAO;
import br.unb.bd.pihole.persistencia.ListaBloqueioDAO;
import br.unb.bd.pihole.util.Formato;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

import java.util.List;

/**
 * Cadastro de listas de bloqueio, com os domínios de cada lista (tabela
 * associativa contem). A data de atualização é mantida pelo banco.
 */
public class ListaBloqueioView extends CadastroView<ListaBloqueio> {

    private final ListaBloqueioDAO dao = new ListaBloqueioDAO();
    private final DominioDAO dominioDAO = new DominioDAO();

    private final TextField campoNome = new TextField();
    private final TextField campoUrl = new TextField();
    private final CheckBox caixaAtiva = new CheckBox("Lista ativa");
    private final SelecaoMultipla<Dominio> dominios =
            new SelecaoMultipla<>("Domínios bloqueados por esta lista:", Dominio::id);

    public ListaBloqueioView() {
        super("Lista de bloqueio");
        coluna("ID", ListaBloqueio::id);
        coluna("Nome", ListaBloqueio::nome);
        coluna("Ativa", l -> Formato.simNao(l.ativa()));
        coluna("Atualizada em", l -> Formato.dataHora(l.dataAtualizacao()));
        coluna("Origem", l -> Formato.texto(l.urlOrigem()));

        campoUrl.setPromptText("opcional (lista manual)");
        campo("Nome:", campoNome);
        campo("URL de origem:", campoUrl);
        extra(caixaAtiva);
        extra(dominios);
    }

    @Override
    protected void carregarOpcoes() {
        dominios.setItens(dominioDAO.listarTodos());
    }

    @Override
    protected List<ListaBloqueio> listar() {
        return dao.listarTodos();
    }

    @Override
    protected void preencherFormulario(ListaBloqueio l) {
        campoNome.setText(l.nome());
        campoUrl.setText(Formato.texto(l.urlOrigem()));
        caixaAtiva.setSelected(l.ativa());
        dominios.marcar(dao.listarDominios(l.id()));
    }

    @Override
    protected void limparCampos() {
        campoNome.clear();
        campoUrl.clear();
        caixaAtiva.setSelected(true);
        dominios.desmarcarTodos();
    }

    @Override
    protected void salvar(ListaBloqueio existente) {
        String nome = obrigatorio(campoNome, "Nome");
        String url = opcional(campoUrl);
        boolean ativa = caixaAtiva.isSelected();

        int idLista;
        if (existente == null) {
            idLista = dao.inserir(new ListaBloqueio(null, nome, null, ativa, url)).id();
        } else {
            idLista = existente.id();
            dao.atualizar(new ListaBloqueio(idLista, nome, null, ativa, url));
        }
        dao.definirDominios(idLista, dominios.getIdsMarcados());
    }

    @Override
    protected void excluir(ListaBloqueio l) {
        dao.excluir(l.id());
    }

    @Override
    protected String descrever(ListaBloqueio l) {
        return "a lista " + l.nome();
    }
}
