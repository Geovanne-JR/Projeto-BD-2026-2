package br.unb.bd.pihole.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/** Caixas de mensagem reutilizadas por todas as telas. */
public final class Dialogos {

    private Dialogos() {
    }

    public static void erro(String mensagem) {
        new Alert(Alert.AlertType.ERROR, mensagem, ButtonType.OK).showAndWait();
    }

    public static void info(String mensagem) {
        new Alert(Alert.AlertType.INFORMATION, mensagem, ButtonType.OK).showAndWait();
    }

    public static boolean confirmar(String mensagem) {
        return new Alert(Alert.AlertType.CONFIRMATION, mensagem, ButtonType.YES, ButtonType.NO)
                .showAndWait()
                .filter(b -> b == ButtonType.YES)
                .isPresent();
    }
}
