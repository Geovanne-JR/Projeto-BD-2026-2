package br.unb.bd.pihole.ui;

import br.unb.bd.pihole.modelo.Alerta;
import br.unb.bd.pihole.persistencia.AlertaDAO;
import br.unb.bd.pihole.persistencia.PersistenciaException;
import br.unb.bd.pihole.util.Formato;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.util.function.Function;

/**
 * Alertas de segurança gerados pelo trigger trg_alerta_requisicao. Os não
 * vistos aparecem primeiro, em negrito.
 */
public class AlertaView extends BorderPane implements Atualizavel {

    private final AlertaDAO dao = new AlertaDAO();
    private final TableView<Alerta> tabela = new TableView<>();

    public AlertaView() {
        coluna("Data/hora", a -> Formato.dataHora(a.dataGeracao()));
        coluna("Gravidade", Alerta::gravidade);
        coluna("Dispositivo", Alerta::nomeDispositivo);
        coluna("Domínio", Alerta::dominio);
        coluna("Visto", a -> Formato.simNao(a.visualizado()));
        coluna("Mensagem", Alerta::mensagem);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabela.setPlaceholder(new Label("Nenhum alerta."));
        tabela.setRowFactory(t -> new TableRow<>() {
            @Override
            protected void updateItem(Alerta item, boolean vazio) {
                super.updateItem(item, vazio);
                setStyle(!vazio && item != null && !item.visualizado() ? "-fx-font-weight: bold;" : "");
            }
        });

        Button btnVisto = new Button("Marcar como visto");
        btnVisto.setOnAction(e -> alterarVisto(true));
        Button btnNaoVisto = new Button("Marcar como não visto");
        btnNaoVisto.setOnAction(e -> alterarVisto(false));
        Button btnExcluir = new Button("Excluir alerta");
        btnExcluir.setOnAction(e -> excluir());
        Button btnAtualizar = new Button("Atualizar");
        btnAtualizar.setOnAction(e -> recarregar());

        HBox botoes = new HBox(8, btnVisto, btnNaoVisto, btnExcluir, btnAtualizar);
        botoes.setPadding(new Insets(10, 0, 0, 0));

        setCenter(tabela);
        setBottom(botoes);
        setPadding(new Insets(10));
    }

    private <V> void coluna(String titulo, Function<Alerta, V> valor) {
        TableColumn<Alerta, V> col = new TableColumn<>(titulo);
        col.setCellValueFactory(d -> new SimpleObjectProperty<>(valor.apply(d.getValue())));
        tabela.getColumns().add(col);
    }

    @Override
    public void recarregar() {
        try {
            tabela.setItems(FXCollections.observableArrayList(dao.listarTodos()));
        } catch (PersistenciaException e) {
            Dialogos.erro(e.getMessage());
        }
    }

    private Alerta selecionado() {
        Alerta a = tabela.getSelectionModel().getSelectedItem();
        if (a == null) {
            Dialogos.erro("Selecione um alerta na tabela.");
        }
        return a;
    }

    private void alterarVisto(boolean visto) {
        Alerta a = selecionado();
        if (a == null) {
            return;
        }
        try {
            dao.marcarVisualizado(a.id(), visto);
            recarregar();
        } catch (PersistenciaException e) {
            Dialogos.erro(e.getMessage());
        }
    }

    private void excluir() {
        Alerta a = selecionado();
        if (a == null || !Dialogos.confirmar("Excluir este alerta?")) {
            return;
        }
        try {
            dao.excluir(a.id());
            recarregar();
        } catch (PersistenciaException e) {
            Dialogos.erro(e.getMessage());
        }
    }
}
