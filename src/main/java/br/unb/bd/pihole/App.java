package br.unb.bd.pihole;

import br.unb.bd.pihole.ui.AlertaView;
import br.unb.bd.pihole.ui.Atualizavel;
import br.unb.bd.pihole.ui.CategoriaDominioView;
import br.unb.bd.pihole.ui.DispositivoView;
import br.unb.bd.pihole.ui.DominioView;
import br.unb.bd.pihole.ui.HistoricoView;
import br.unb.bd.pihole.ui.ListaBloqueioView;
import br.unb.bd.pihole.ui.PerfilView;
import br.unb.bd.pihole.ui.RedeLocalView;
import br.unb.bd.pihole.ui.RegraView;
import br.unb.bd.pihole.ui.ServidorDnsView;
import br.unb.bd.pihole.ui.UsuarioView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

/**
 * Janela principal: uma aba por cadastro. Sempre que o usuário troca de
 * aba, a tela da aba é recarregada do banco.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        TabPane abas = new TabPane(
                aba("Dispositivos", new DispositivoView()),
                aba("Redes", new RedeLocalView()),
                aba("Servidores DNS", new ServidorDnsView()),
                aba("Listas de bloqueio", new ListaBloqueioView()),
                aba("Domínios", new DominioView()),
                aba("Categorias", new CategoriaDominioView()),
                aba("Regras", new RegraView()),
                aba("Histórico DNS", new HistoricoView()),
                aba("Alertas", new AlertaView()),
                aba("Usuários", new UsuarioView()),
                aba("Perfis", new PerfilView()));

        abas.getSelectionModel().selectedItemProperty().addListener((obs, antiga, nova) -> recarregar(nova));

        stage.setTitle("Monitoramento de Rede Doméstica e Bloqueio de Anúncios");
        stage.setScene(new Scene(abas, 1150, 650));
        stage.show();

        // Carrega a primeira aba depois que a janela aparece, para que uma
        // mensagem de erro (ex.: banco fora do ar) apareça sobre ela.
        Platform.runLater(() -> recarregar(abas.getSelectionModel().getSelectedItem()));
    }

    private static Tab aba(String titulo, Node conteudo) {
        Tab tab = new Tab(titulo, conteudo);
        tab.setClosable(false);
        return tab;
    }

    private static void recarregar(Tab tab) {
        if (tab != null && tab.getContent() instanceof Atualizavel tela) {
            tela.recarregar();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
