package br.unb.bd.pihole.ui;

import br.unb.bd.pihole.modelo.Perfil;
import br.unb.bd.pihole.modelo.Usuario;
import br.unb.bd.pihole.persistencia.PerfilDAO;
import br.unb.bd.pihole.persistencia.UsuarioDAO;
import br.unb.bd.pihole.util.Senha;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Cadastro de usuários. Além do CRUD, mostra:
 * - duas tabelas relacionadas: o perfil vem de um ComboBox carregado de
 *   perfis, e a tabela exibe o nome do perfil via JOIN;
 * - dado binário: a foto é lida do disco e gravada em foto_perfil (BYTEA).
 */
public class UsuarioView extends CadastroView<Usuario> {

    private static final long TAMANHO_MAX_FOTO = 2 * 1024 * 1024; // 2 MB

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final PerfilDAO perfilDAO = new PerfilDAO();

    private final TextField campoNome = new TextField();
    private final TextField campoEmail = new TextField();
    private final PasswordField campoSenha = new PasswordField();
    private final ComboBox<Perfil> comboPerfil = new ComboBox<>();
    private final ImageView imagemFoto = new ImageView();

    private byte[] fotoAtual;
    private boolean fotoAlterada;

    public UsuarioView() {
        super("Usuário");
        coluna("ID", Usuario::getIdUsuario);
        coluna("Nome", Usuario::getNome);
        coluna("E-mail", Usuario::getEmail);
        coluna("Perfil", Usuario::getNomePerfil);

        campo("Nome:", campoNome);
        campo("E-mail:", campoEmail);
        campo("Senha:", campoSenha);
        campo("Perfil:", comboPerfil);

        imagemFoto.setFitWidth(120);
        imagemFoto.setFitHeight(120);
        imagemFoto.setPreserveRatio(true);
        Button btnFoto = new Button("Escolher foto...");
        btnFoto.setOnAction(e -> escolherFoto());
        Button btnSemFoto = new Button("Remover foto");
        btnSemFoto.setOnAction(e -> definirFoto(null, true));
        extra(new Label("Foto de perfil:"));
        extra(imagemFoto);
        extra(new HBox(8, btnFoto, btnSemFoto));
    }

    @Override
    protected void carregarOpcoes() {
        opcoes(comboPerfil, perfilDAO.listarTodos());
    }

    @Override
    protected List<Usuario> listar() {
        return usuarioDAO.listarTodos();
    }

    @Override
    protected void preencherFormulario(Usuario linha) {
        // A listagem não traz a foto; busca o registro completo.
        Usuario u = usuarioDAO.buscarPorId(linha.getIdUsuario()).orElse(linha);
        campoNome.setText(u.getNome());
        campoEmail.setText(u.getEmail());
        campoSenha.clear();
        campoSenha.setPromptText("deixe vazio para manter");
        selecionar(comboPerfil, p -> p.id() == u.getIdPerfil());
        definirFoto(u.getFotoPerfil(), false);
    }

    @Override
    protected void limparCampos() {
        campoNome.clear();
        campoEmail.clear();
        campoSenha.clear();
        campoSenha.setPromptText("");
        comboPerfil.setValue(null);
        definirFoto(null, false);
    }

    @Override
    protected void salvar(Usuario existente) {
        String nome = obrigatorio(campoNome, "Nome");
        String email = obrigatorio(campoEmail, "E-mail");
        Perfil perfil = obrigatorio(comboPerfil, "Perfil");
        String senha = campoSenha.getText();
        if (existente == null && senha.isEmpty()) {
            throw new IllegalArgumentException("Informe uma senha para o novo usuário.");
        }

        Usuario u = existente == null ? new Usuario() : existente;
        u.setNome(nome);
        u.setEmail(email);
        u.setIdPerfil(perfil.id());
        if (!senha.isEmpty()) {
            u.setSenhaHash(Senha.gerarHash(senha));
        }

        if (existente == null) {
            u.setFotoPerfil(fotoAtual);
            usuarioDAO.inserir(u);
        } else {
            usuarioDAO.atualizar(u);
            if (fotoAlterada) {
                usuarioDAO.atualizarFoto(u.getIdUsuario(), fotoAtual);
            }
        }
    }

    @Override
    protected void excluir(Usuario u) {
        usuarioDAO.excluir(u.getIdUsuario());
    }

    @Override
    protected String descrever(Usuario u) {
        return "o usuário " + u.getNome();
    }

    private void escolherFoto() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Escolha a foto de perfil");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File arquivo = fc.showOpenDialog(getScene().getWindow());
        if (arquivo == null) {
            return;
        }
        if (arquivo.length() > TAMANHO_MAX_FOTO) {
            Dialogos.erro("A foto deve ter no máximo 2 MB.");
            return;
        }
        try {
            definirFoto(Files.readAllBytes(arquivo.toPath()), true);
        } catch (IOException e) {
            Dialogos.erro("Não foi possível ler o arquivo: " + e.getMessage());
        }
    }

    private void definirFoto(byte[] bytes, boolean alterada) {
        fotoAtual = bytes;
        fotoAlterada = alterada;
        imagemFoto.setImage(bytes == null ? null : new Image(new ByteArrayInputStream(bytes)));
    }
}
