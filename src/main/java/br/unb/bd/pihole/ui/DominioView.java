package br.unb.bd.pihole.ui;

import br.unb.bd.pihole.modelo.CategoriaDominio;
import br.unb.bd.pihole.modelo.Dominio;
import br.unb.bd.pihole.persistencia.CategoriaDominioDAO;
import br.unb.bd.pihole.persistencia.DominioDAO;
import br.unb.bd.pihole.util.Formato;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.Objects;

/** Cadastro de domínios. A categoria é opcional. */
public class DominioView extends CadastroView<Dominio> {

    private final DominioDAO dao = new DominioDAO();
    private final CategoriaDominioDAO categoriaDAO = new CategoriaDominioDAO();
    private final TextField campoNome = new TextField();
    private final ComboBox<CategoriaDominio> comboCategoria = new ComboBox<>();

    public DominioView() {
        super("Domínio");
        coluna("ID", Dominio::id);
        coluna("Domínio", Dominio::nome);
        coluna("Categoria", d -> Formato.texto(d.nomeCategoria()));

        campoNome.setPromptText("ex.: ads.exemplo.com");
        comboCategoria.setPromptText("(sem categoria)");
        campo("Domínio:", campoNome);
        campo("Categoria:", comBotaoLimpar(comboCategoria, "Sem categoria"));
    }

    @Override
    protected void carregarOpcoes() {
        opcoes(comboCategoria, categoriaDAO.listarTodos());
    }

    @Override
    protected List<Dominio> listar() {
        return dao.listarTodos();
    }

    @Override
    protected void preencherFormulario(Dominio d) {
        campoNome.setText(d.nome());
        selecionar(comboCategoria, c -> Objects.equals(c.id(), d.idCategoria()));
    }

    @Override
    protected void limparCampos() {
        campoNome.clear();
        comboCategoria.setValue(null);
    }

    @Override
    protected void salvar(Dominio existente) {
        String nome = obrigatorio(campoNome, "Domínio").toLowerCase();
        CategoriaDominio cat = comboCategoria.getValue();
        Integer idCat = cat == null ? null : cat.id();
        String nomeCat = cat == null ? null : cat.nome();
        if (existente == null) {
            dao.inserir(new Dominio(null, idCat, nomeCat, nome));
        } else {
            dao.atualizar(new Dominio(existente.id(), idCat, nomeCat, nome));
        }
    }

    @Override
    protected void excluir(Dominio d) {
        dao.excluir(d.id());
    }

    @Override
    protected String descrever(Dominio d) {
        return "o domínio " + d.nome();
    }
}
