package br.unb.bd.pihole.ui;

/**
 * Tela que sabe recarregar seus dados do banco. A janela principal chama
 * {@link #recarregar()} sempre que o usuário abre a aba, para que as
 * listas (ComboBox, tabelas) mostrem o que foi cadastrado em outras abas.
 */
public interface Atualizavel {
    void recarregar();
}
