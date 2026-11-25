package br.unb.bd.pihole.ui;

import br.unb.bd.pihole.modelo.ServidorDns;
import br.unb.bd.pihole.persistencia.ServidorDnsDAO;
import br.unb.bd.pihole.util.Formato;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

import java.util.List;

/** Cadastro de servidores DNS externos. */
public class ServidorDnsView extends CadastroView<ServidorDns> {

    private final ServidorDnsDAO dao = new ServidorDnsDAO();
    private final TextField campoNome = new TextField();
    private final TextField campoIpPrimario = new TextField();
    private final TextField campoIpSecundario = new TextField();
    private final CheckBox caixaDoh = new CheckBox("Suporta DNS over HTTPS (DoH)");

    public ServidorDnsView() {
        super("Servidor DNS");
        coluna("ID", ServidorDns::id);
        coluna("Nome", ServidorDns::nome);
        coluna("IP primário", ServidorDns::ipPrimario);
        coluna("IP secundário", s -> Formato.texto(s.ipSecundario()));
        coluna("DoH", s -> Formato.simNao(s.suportaDoh()));

        campoIpPrimario.setPromptText("ex.: 1.1.1.1");
        campoIpSecundario.setPromptText("opcional");
        campo("Nome:", campoNome);
        campo("IP primário:", campoIpPrimario);
        campo("IP secundário:", campoIpSecundario);
        extra(caixaDoh);
    }

    @Override
    protected List<ServidorDns> listar() {
        return dao.listarTodos();
    }

    @Override
    protected void preencherFormulario(ServidorDns s) {
        campoNome.setText(s.nome());
        campoIpPrimario.setText(s.ipPrimario());
        campoIpSecundario.setText(Formato.texto(s.ipSecundario()));
        caixaDoh.setSelected(s.suportaDoh());
    }

    @Override
    protected void limparCampos() {
        campoNome.clear();
        campoIpPrimario.clear();
        campoIpSecundario.clear();
        caixaDoh.setSelected(false);
    }

    @Override
    protected void salvar(ServidorDns existente) {
        String nome = obrigatorio(campoNome, "Nome");
        String ip1 = obrigatorio(campoIpPrimario, "IP primário");
        String ip2 = opcional(campoIpSecundario);
        boolean doh = caixaDoh.isSelected();
        if (existente == null) {
            dao.inserir(new ServidorDns(null, nome, ip1, ip2, doh));
        } else {
            dao.atualizar(new ServidorDns(existente.id(), nome, ip1, ip2, doh));
        }
    }

    @Override
    protected void excluir(ServidorDns s) {
        dao.excluir(s.id());
    }

    @Override
    protected String descrever(ServidorDns s) {
        return "o servidor " + s.nome();
    }
}
