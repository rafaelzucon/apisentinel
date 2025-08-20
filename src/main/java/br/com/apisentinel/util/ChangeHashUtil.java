package br.com.apisentinel.util;

import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;

public final class ChangeHashUtil {

    private ChangeHashUtil() {
        // util class
    }

    /**
     * Calcula o hash HMAC-SHA256 a partir da chave e campos relevantes.
     *
     * @param hmacKey chave secreta em bytes (configurada em ApiSentinelProperties)
     * @param name nome normalizado do asset
     * @param version versão do asset (use gwVersion, como no legado)
     * @param context contexto/basePath
     * @param description descrição do asset
     * @return hash em String (hex)
     */
    public static String compute(byte[] hmacKey,
                                 String name,
                                 String version,
                                 String context,
                                 String description) {
        // evita NPE — concatena vazio se null
        String base = String.join("##",
                safe(name),
                safe(version),
                safe(context),
                safe(description)
        );

        return Hashing.hmacSha256(hmacKey)
                .hashBytes(base.getBytes(StandardCharsets.UTF_8))
                .toString();
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
