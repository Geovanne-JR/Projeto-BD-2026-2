package br.unb.bd.pihole.ui;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Lista de caixas de seleção, usada para editar as tabelas associativas
 * (ex.: quais listas de bloqueio valem para um dispositivo).
 *
 * @param <T> tipo dos itens (ex.: ListaBloqueio)
 */
public class SelecaoMultipla<T> extends VBox {

    private final Function<T, Integer> id;
    private final VBox caixas = new VBox(4);
    private final Map<CheckBox, T> itens = new LinkedHashMap<>();

    /**
     * @param titulo texto exibido acima das caixas
     * @param id     como obter o ID (chave) de um item
     */
    public SelecaoMultipla(String titulo, Function<T, Integer> id) {
        super(4);
        this.id = id;
        ScrollPane rolagem = new ScrollPane(caixas);
        rolagem.setFitToWidth(true);
        rolagem.setPrefViewportHeight(150);
        getChildren().addAll(new Label(titulo), rolagem);
    }

    public void setItens(List<T> lista) {
        caixas.getChildren().clear();
        itens.clear();
        for (T item : lista) {
            CheckBox cb = new CheckBox(item.toString());
            itens.put(cb, item);
            caixas.getChildren().add(cb);
        }
    }

    /** Marca exatamente os itens cujos IDs estão em {@code ids}. */
    public void marcar(Collection<Integer> ids) {
        itens.forEach((cb, item) -> cb.setSelected(ids.contains(id.apply(item))));
    }

    public void desmarcarTodos() {
        itens.keySet().forEach(cb -> cb.setSelected(false));
    }

    public List<Integer> getIdsMarcados() {
        List<Integer> ids = new ArrayList<>();
        itens.forEach((cb, item) -> {
            if (cb.isSelected()) {
                ids.add(id.apply(item));
            }
        });
        return ids;
    }
}
