package br.unb.bd.pihole.ui;

import br.unb.bd.pihole.modelo.CategoriaDominio;
import br.unb.bd.pihole.persistencia.CategoriaDominioDAO;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.util.List;

/** Cadastro de categorias de domínio e seu nível de risco. */
public class CategoriaDominioView extends CadastroView<CategoriaDominio> {

    private final CategoriaDominioDAO dao = new CategoriaDominioDAO();
    private final TextField campoNome = new TextField();
    private final ComboBox<String> comboRisco = new ComboBox<>();

    public CategoriaDominioView() {
        super("Categoria de domínio");
        coluna("ID", CategoriaDominio::id);
        coluna("Nome", CategoriaDominio::nome);
        coluna("Nível de risco", CategoriaDominio::nivelRisco);

        comboRisco.getItems().setAll(CategoriaDominio.NIVEIS_RISCO);
        campo("Nome:", campoNome);
        campo("Nível de risco:", comboRisco);
    }

    @Override
    protected List<CategoriaDominio> listar() {
        return dao.listarTodos();
    }

    @Override
    protected void preencherFormulario(CategoriaDominio c) {
        campoNome.setText(c.nome());
        comboRisco.setValue(c.nivelRisco());
    }

    @Override
    protected void limparCampos() {
        campoNome.clear();
        comboRisco.setValue(null);
    }

    @Override
    protected void salvar(CategoriaDominio existente) {
        String nome = obrigatorio(campoNome, "Nome");
        String risco = obrigatorio(comboRisco, "Nível de risco");
        if (existente == null) {
            dao.inserir(new CategoriaDominio(null, nome, risco));
        } else {
            dao.atualizar(new CategoriaDominio(existente.id(), nome, risco));
        }
    }

    @Override
    protected void excluir(CategoriaDominio c) {
        dao.excluir(c.id());
    }

    @Override
    protected String descrever(CategoriaDominio c) {
        return "a categoria " + c.nome() + " (os domínios dela ficarão sem categoria)";
    }
}
