package br.unb.bd.pihole.modelo;

/**
 * Linha da tabela usuarios (camada de mapeamento objeto-relacional).
 *
 * <pre>
 * usuarios.id_usuario   INT  (identity) -> idUsuario
 * usuarios.idperfis     INT  (FK)       -> idPerfil
 * usuarios.nome         VARCHAR(100)    -> nome
 * usuarios.email        VARCHAR(150)    -> email
 * usuarios.foto_perfil  BYTEA           -> fotoPerfil (byte[])
 * usuarios.senha_hash   VARCHAR(255)    -> senhaHash
 * perfis.nomeperfil     (via JOIN)      -> nomePerfil (só leitura)
 * </pre>
 */
public class Usuario {

    private Integer idUsuario;   // null enquanto não foi inserido
    private int idPerfil;
    private String nome;
    private String email;
    private byte[] fotoPerfil;   // pode ser null
    private String senhaHash;
    private String nomePerfil;   // preenchido pelo JOIN, não é gravado

    public Usuario() {
    }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public int getIdPerfil() { return idPerfil; }
    public void setIdPerfil(int idPerfil) { this.idPerfil = idPerfil; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public byte[] getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(byte[] fotoPerfil) { this.fotoPerfil = fotoPerfil; }

    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }

    public String getNomePerfil() { return nomePerfil; }
    public void setNomePerfil(String nomePerfil) { this.nomePerfil = nomePerfil; }

    /** Texto exibido nos ComboBox (ex.: dono de uma rede). */
    @Override
    public String toString() {
        return nome;
    }
}
