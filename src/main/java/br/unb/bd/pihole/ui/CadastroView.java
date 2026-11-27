package br.unb.bd.pihole.ui;

import br.unb.bd.pihole.persistencia.PersistenciaException;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Base das telas de cadastro: tabela com os registros à esquerda e
 * formulário à direita, com os botões Novo, Salvar e Excluir.
 *
 * Cada tela concreta (UsuarioView, DispositivoView...) só diz quais são as
 * colunas e os campos, e como ler/gravar pelo DAO. O fluxo (selecionar
 * linha, salvar, excluir com confirmação, mostrar erros) fica aqui.
 *
 * Fluxo do "Salvar": se nenhuma linha está selecionada, é uma inserção;
 * se há uma linha selecionada, é uma atualização dela.
 *
 * @param <T> classe do modelo exibida na tabela
 */
public abstract class CadastroView<T> extends BorderPane implements Atualizavel {

    protected final TableView<T> tabela = new TableView<>();
    private final GridPane grade = new GridPane();
    private final VBox formulario = new VBox(10);
    private int linhas = 0;

    /** Registro selecionado na tabela; null quando o formulário é de um novo registro. */
    protected T selecionado;

    protected CadastroView(String tituloFormulario) {
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabela.setPlaceholder(new Label("Nenhum registro cadastrado."));
        tabela.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                selecionado = novo;
                try {
                    preencherFormulario(novo);
                } catch (PersistenciaException e) {
                    Dialogos.erro(e.getMessage());
                }
            }
        });

        grade.setHgap(8);
        grade.setVgap(8);
        ColumnConstraints rotulos = new ColumnConstraints();
        ColumnConstraints campos = new ColumnConstraints();
        campos.setHgrow(Priority.ALWAYS);
        campos.setFillWidth(true);
        grade.getColumnConstraints().addAll(rotulos, campos);

        Label titulo = new Label(tituloFormulario);
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        formulario.getChildren().addAll(titulo, grade);

        Button btnNovo = new Button("Novo");
        btnNovo.setOnAction(e -> limpar());
        Button btnSalvar = new Button("Salvar");
        btnSalvar.setDefaultButton(true);
        btnSalvar.setOnAction(e -> acaoSalvar());
        Button btnExcluir = new Button("Excluir");
        btnExcluir.setOnAction(e -> acaoExcluir());

        VBox lateral = new VBox(14, formulario, new HBox(8, btnNovo, btnSalvar, btnExcluir));
        lateral.setPadding(new Insets(4, 12, 4, 4));
        ScrollPane rolagem = new ScrollPane(lateral);
        rolagem.setFitToWidth(true);
        rolagem.setPrefWidth(380);
        rolagem.setMinWidth(320);

        setCenter(tabela);
        setRight(rolagem);
        setPadding(new Insets(10));
        BorderPane.setMargin(rolagem, new Insets(0, 0, 0, 10));
    }

    // ------------------------------------------------------------------
    // O que cada tela concreta implementa
    // ------------------------------------------------------------------

    /** Busca os registros exibidos na tabela. */
    protected abstract List<T> listar();

    /** Copia o registro selecionado para os campos do formulário. */
    protected abstract void preencherFormulario(T item);

    /** Deixa o formulário em branco (novo registro). */
    protected abstract void limparCampos();

    /**
     * Lê o formulário e grava pelo DAO.
     *
     * @param existente registro selecionado (atualização) ou null (inserção)
     * @throws IllegalArgumentException com a mensagem para o usuário, se
     *         algum campo estiver inválido
     */
    protected abstract void salvar(T existente);

    protected abstract void excluir(T item);

    /** Nome do registro usado na pergunta "Excluir ...?". */
    protected abstract String descrever(T item);

    /** Carrega as opções dos ComboBox (ex.: lista de usuários). Opcional. */
    protected void carregarOpcoes() {
    }

    // ------------------------------------------------------------------
    // Montagem da tela (usado no construtor das telas concretas)
    // ------------------------------------------------------------------

    protected <V> void coluna(String titulo, Function<T, V> valor) {
        TableColumn<T, V> col = new TableColumn<>(titulo);
        col.setCellValueFactory(d -> new SimpleObjectProperty<>(valor.apply(d.getValue())));
        tabela.getColumns().add(col);
    }

    protected void campo(String rotulo, Node controle) {
        if (controle instanceof Region r) {
            r.setMaxWidth(Double.MAX_VALUE);
        }
        grade.addRow(linhas++, new Label(rotulo), controle);
    }

    /** Adiciona um componente abaixo dos campos (ex.: foto, SelecaoMultipla). */
    protected void extra(Node componente) {
        formulario.getChildren().add(componente);
    }

    // ------------------------------------------------------------------
    // Fluxo
    // ------------------------------------------------------------------

    @Override
    public void recarregar() {
        try {
            carregarOpcoes();
            tabela.setItems(FXCollections.observableArrayList(listar()));
        } catch (PersistenciaException e) {
            Dialogos.erro(e.getMessage());
        }
        limpar();
    }

    private void limpar() {
        selecionado = null;
        tabela.getSelectionModel().clearSelection();
        limparCampos();
    }

    private void acaoSalvar() {
        try {
            salvar(selecionado);
            recarregar();
        } catch (IllegalArgumentException | PersistenciaException e) {
            Dialogos.erro(e.getMessage());
        }
    }

    private void acaoExcluir() {
        if (selecionado == null) {
            Dialogos.erro("Selecione um registro na tabela.");
            return;
        }
        if (!Dialogos.confirmar("Excluir " + descrever(selecionado) + "?")) {
            return;
        }
        try {
            excluir(selecionado);
            recarregar();
        } catch (PersistenciaException e) {
            Dialogos.erro(e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Validação e utilidades dos campos
    // ------------------------------------------------------------------

    /** Texto do campo sem espaços nas pontas; erro se estiver vazio. */
    protected static String obrigatorio(TextInputControl campo, String nome) {
        String valor = campo.getText() == null ? "" : campo.getText().trim();
        if (valor.isEmpty()) {
            throw new IllegalArgumentException("Preencha o campo \"" + nome + "\".");
        }
        return valor;
    }

    /** Texto do campo, ou null se estiver vazio (grava NULL no banco). */
    protected static String opcional(TextInputControl campo) {
        String valor = campo.getText() == null ? "" : campo.getText().trim();
        return valor.isEmpty() ? null : valor;
    }

    protected static <X> X obrigatorio(ComboBox<X> combo, String nome) {
        X valor = combo.getValue();
        if (valor == null) {
            throw new IllegalArgumentException("Escolha um valor em \"" + nome + "\".");
        }
        return valor;
    }

    /** Número inteiro opcional; erro se não for um número. */
    protected static Integer inteiroOpcional(TextInputControl campo, String nome) {
        String valor = opcional(campo);
        if (valor == null) {
            return null;
        }
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("O campo \"" + nome + "\" deve ser um número inteiro.");
        }
    }

    /** Seleciona no ComboBox o primeiro item que atende ao critério (ou nenhum). */
    protected static <X> void selecionar(ComboBox<X> combo, Predicate<X> criterio) {
        combo.setValue(combo.getItems().stream().filter(criterio).findFirst().orElse(null));
    }

    protected static <X> void opcoes(ComboBox<X> combo, List<X> itens) {
        combo.setItems(FXCollections.observableArrayList(itens));
    }

    /** Botão que limpa um ComboBox opcional (ex.: "Sem dono"). */
    protected static Node comBotaoLimpar(ComboBox<?> combo, String textoBotao) {
        Button limpar = new Button(textoBotao);
        limpar.setOnAction(e -> combo.setValue(null));
        combo.setMaxWidth(Double.MAX_VALUE);
        HBox caixa = new HBox(6, combo, limpar);
        HBox.setHgrow(combo, Priority.ALWAYS);
        return caixa;
    }
}
