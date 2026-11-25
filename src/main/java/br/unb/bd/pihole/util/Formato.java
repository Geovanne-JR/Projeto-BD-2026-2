package br.unb.bd.pihole.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Formatação de valores para exibição nas telas. */
public final class Formato {

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Formato() {
    }

    public static String dataHora(LocalDateTime valor) {
        return valor == null ? "" : valor.format(DATA_HORA);
    }

    public static String simNao(boolean valor) {
        return valor ? "Sim" : "Não";
    }

    /** Texto vazio no lugar de null (para colunas opcionais). */
    public static String texto(Object valor) {
        return valor == null ? "" : valor.toString();
    }
}
