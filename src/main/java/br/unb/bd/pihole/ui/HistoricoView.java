package br.unb.bd.pihole.ui;

import br.unb.bd.pihole.modelo.Dispositivo;
import br.unb.bd.pihole.modelo.Dominio;
import br.unb.bd.pihole.modelo.RegistroRequisicao;
import br.unb.bd.pihole.persistencia.DispositivoDAO;
import br.unb.bd.pihole.persistencia.DominioDAO;
import br.unb.bd.pihole.persistencia.PersistenciaException;
import br.unb.bd.pihole.persistencia.RequisicaoDAO;
import br.unb.bd.pihole.util.Formato;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

/**
 * Histórico de requisições DNS, lido da VIEW vw_historico_requisicoes.
 *
 * O painel de cima simula uma requisição: chama a PROCEDURE
 * registrar_requisicao, que decide se ela é permitida ou bloqueada. Se for
 * bloqueada e o domínio for de alto risco, o TRIGGER cria um alerta (veja a
 * aba Alertas).
 */
public class HistoricoView extends BorderPane implements Atualizavel {

    private static final int LIMITE = 500;

    private final RequisicaoDAO dao = new RequisicaoDAO();
    private final DispositivoDAO dispositivoDAO = new DispositivoDAO();
    private final DominioDAO dominioDAO = new DominioDAO();

    private final TableView<RegistroRequisicao> tabela = new TableView<>();
    private final ComboBox<Dispositivo> comboDispositivo = new ComboBox<>();
    private final ComboBox<Dominio> comboDominio = new ComboBox<>();

    public HistoricoView() {
        coluna("Data/hora", r -> Formato.dataHora(r.dataHora()));
        coluna("Dispositivo", RegistroRequisicao::nomeDispositivo);
        coluna("Dono", r -> Formato.texto(r.dono()));
        coluna("Domínio", RegistroRequisicao::dominio);
        coluna("Categoria", r -> Formato.texto(r.categoria()));
        coluna("Risco", r -> Formato.texto(r.nivelRisco()));
        coluna("Status", RegistroRequisicao::status);
        coluna("Tempo (ms)", r -> Formato.texto(r.tempoRespostaMs()));
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabela.setPlaceholder(new Label("Nenhuma requisição registrada."));
        tabela.setRowFactory(t -> new TableRow<>() {
            @Override
            protected void updateItem(RegistroRequisicao item, boolean vazio) {
                super.updateItem(item, vazio);
                setStyle(!vazio && item != null && "bloqueada".equals(item.status())
                        ? "-fx-background-color: #fde2e2;" : "");
            }
        });

        comboDispositivo.setPromptText("Dispositivo");
        comboDominio.setPromptText("Domínio");
        Button btnSimular = new Button("Simular requisição");
        btnSimular.setOnAction(e -> simular());
        Button btnAtualizar = new Button("Atualizar");
        btnAtualizar.setOnAction(e -> recarregar());

        HBox painel = new HBox(8, new Label("Simular:"), comboDispositivo, comboDominio, btnSimular, btnAtualizar);
        HBox.setHgrow(comboDispositivo, Priority.SOMETIMES);
        HBox.setHgrow(comboDominio, Priority.SOMETIMES);
        painel.setPadding(new Insets(0, 0, 10, 0));
        painel.setStyle("-fx-alignment: center-left;");

        setTop(painel);
        setCenter(tabela);
        setPadding(new Insets(10));
    }

    private <V> void coluna(String titulo, Function<RegistroRequisicao, V> valor) {
        TableColumn<RegistroRequisicao, V> col = new TableColumn<>(titulo);
        col.setCellValueFactory(d -> new SimpleObjectProperty<>(valor.apply(d.getValue())));
        tabela.getColumns().add(col);
    }

    @Override
    public void recarregar() {
        try {
            comboDispositivo.setItems(FXCollections.observableArrayList(dispositivoDAO.listarTodos()));
            comboDominio.setItems(FXCollections.observableArrayList(dominioDAO.listarTodos()));
            tabela.setItems(FXCollections.observableArrayList(dao.listarHistorico(LIMITE)));
        } catch (PersistenciaException e) {
            Dialogos.erro(e.getMessage());
        }
    }

    private void simular() {
        Dispositivo disp = comboDispositivo.getValue();
        Dominio dom = comboDominio.getValue();
        if (disp == null || dom == null) {
            Dialogos.erro("Escolha o dispositivo e o domínio.");
            return;
        }
        try {
            int tempo = ThreadLocalRandom.current().nextInt(10, 80);
            String status = dao.registrar(disp.mac(), dom.id(), tempo);
            tabela.setItems(FXCollections.observableArrayList(dao.listarHistorico(LIMITE)));
            Dialogos.info("Requisição de " + disp.nome() + " para " + dom.nome() + ": " + status.toUpperCase() + ".");
        } catch (PersistenciaException e) {
            Dialogos.erro(e.getMessage());
        }
    }
}
