package br.unb.bd.pihole.ui;

import br.unb.bd.pihole.modelo.Perfil;
import br.unb.bd.pihole.persistencia.PerfilDAO;
import javafx.scene.control.TextField;

import java.util.List;

/** Cadastro de perfis de acesso. */
public class PerfilView extends CadastroView<Perfil> {

    private final PerfilDAO dao = new PerfilDAO();
    private final TextField campoNome = new TextField();
    private final TextField campoDescricao = new TextField();

    public PerfilView() {
        super("Perfil de acesso");
        coluna("ID", Perfil::id);
        coluna("Nome", Perfil::nome);
        coluna("Descrição", Perfil::descricao);

        campo("Nome:", campoNome);
        campo("Descrição:", campoDescricao);
    }

    @Override
    protected List<Perfil> listar() {
        return dao.listarTodos();
    }

    @Override
    protected void preencherFormulario(Perfil p) {
        campoNome.setText(p.nome());
        campoDescricao.setText(p.descricao());
    }

    @Override
    protected void limparCampos() {
        campoNome.clear();
        campoDescricao.clear();
    }

    @Override
    protected void salvar(Perfil existente) {
        String nome = obrigatorio(campoNome, "Nome");
        String descricao = opcional(campoDescricao);
        if (existente == null) {
            dao.inserir(new Perfil(null, nome, descricao));
        } else {
            dao.atualizar(new Perfil(existente.id(), nome, descricao));
        }
    }

    @Override
    protected void excluir(Perfil p) {
        dao.excluir(p.id());
    }

    @Override
    protected String descrever(Perfil p) {
        return "o perfil " + p.nome();
    }
}
