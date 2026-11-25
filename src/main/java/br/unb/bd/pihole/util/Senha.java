package br.unb.bd.pihole.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Gera e confere o valor da coluna senha_hash (PBKDF2, já incluso no JDK).
 * Formato gravado: pbkdf2$iteracoes$sal$hash (sal e hash em Base64).
 */
public final class Senha {

    private static final int ITERACOES = 120_000;
    private static final SecureRandom RANDOM = new SecureRandom();

    private Senha() {
    }

    public static String gerarHash(String senha) {
        byte[] sal = new byte[16];
        RANDOM.nextBytes(sal);
        byte[] hash = pbkdf2(senha, sal, ITERACOES);
        Base64.Encoder b64 = Base64.getEncoder();
        return "pbkdf2$" + ITERACOES + "$" + b64.encodeToString(sal) + "$" + b64.encodeToString(hash);
    }

    public static boolean confere(String senha, String hashGravado) {
        String[] partes = hashGravado.split("\\$");
        if (partes.length != 4 || !partes[0].equals("pbkdf2")) {
            return false;
        }
        Base64.Decoder b64 = Base64.getDecoder();
        byte[] esperado = b64.decode(partes[3]);
        byte[] calculado = pbkdf2(senha, b64.decode(partes[2]), Integer.parseInt(partes[1]));
        return MessageDigest.isEqual(esperado, calculado);
    }

    private static byte[] pbkdf2(String senha, byte[] sal, int iteracoes) {
        try {
            PBEKeySpec spec = new PBEKeySpec(senha.toCharArray(), sal, iteracoes, 256);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
