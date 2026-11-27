package br.unb.bd.pihole;

/**
 * Ponto de entrada do programa.
 *
 * Fica separado de {@link App} porque a JVM se recusa a iniciar uma classe
 * que estende {@code Application} quando o JavaFX está no classpath
 * ("JavaFX runtime components are missing"). Chamando por aqui, funciona
 * tanto pelo Maven quanto pelo botão "Run" da IDE.
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
