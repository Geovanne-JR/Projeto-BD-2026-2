package br.unb.bd.pihole.persistencia;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Fornece conexões com o PostgreSQL.
 *
 * Cada operação abre a sua conexão e a fecha no fim
 * (try-with-resources). Isso evita conexão "pendurada" se o servidor cair e
 * é o padrão recomendado com JDBC.
 *
 * Os dados de acesso ficam em src/main/resources/db.properties.
 */
public final class ConexaoBD {

    private static Properties config;

    private ConexaoBD() {
    }

    public static Connection getConexao() throws SQLException {
        Properties p = carregarConfig();
        return DriverManager.getConnection(
                p.getProperty("db.url"),
                p.getProperty("db.usuario"),
                p.getProperty("db.senha"));
    }

    private static synchronized Properties carregarConfig() {
        if (config == null) {
            try (InputStream in = ConexaoBD.class.getResourceAsStream("/db.properties")) {
                if (in == null) {
                    throw new PersistenciaException(
                            "Arquivo db.properties não encontrado. Copie "
                            + "src/main/resources/db.properties.example para db.properties.");
                }
                Properties p = new Properties();
                p.load(in);
                config = p;
            } catch (IOException e) {
                throw new PersistenciaException("Erro ao ler db.properties: " + e.getMessage(), e);
            }
        }
        return config;
    }
}
