package br.unb.bd.pihole.persistencia;

import java.sql.SQLException;

/**
 * Erro da camada de persistência, já com uma mensagem que um usuário leigo
 * entende. A interface só precisa exibir {@link #getMessage()}.
 *
 * Os códigos SQLState são do padrão SQL e listados em
 * https://www.postgresql.org/docs/current/errcodes-appendix.html
 */
public class PersistenciaException extends RuntimeException {

    public PersistenciaException(String mensagem) {
        super(mensagem);
    }

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

    /** Converte a exceção do JDBC numa mensagem amigável. */
    public static PersistenciaException de(SQLException e) {
        String estado = e.getSQLState() == null ? "" : e.getSQLState();
        String mensagem = switch (estado) {
            case "23505" -> "Já existe um registro com esse valor (campo que não pode repetir).";
            case "23503" -> "Operação não permitida: o registro está ligado a outros dados "
                    + "ou aponta para um registro que não existe.";
            case "23502" -> "Preencha todos os campos obrigatórios.";
            case "23514" -> "Um dos valores informados não é permitido.";
            case "22P02" -> "Formato inválido (verifique IP, MAC ou números).";
            case "22001" -> "Um dos textos é maior que o tamanho permitido.";
            default -> estado.startsWith("08")
                    ? "Não foi possível conectar ao banco de dados."
                    : "Erro no banco de dados: " + e.getMessage();
        };
        return new PersistenciaException(mensagem, e);
    }
}
