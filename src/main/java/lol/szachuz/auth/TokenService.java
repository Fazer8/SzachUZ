package lol.szachuz.auth;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import lol.szachuz.db.Entities.Users;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class TokenService {

    private static final String PRIVATE_KEY_LOCATION = "/META-INF/privateKey.pem";
    private static final String ISSUER = "https://szachuz.lol/issuer";
    private static final long EXPIRATION_SECONDS = 3600;

    private PrivateKey privateKey;

    public TokenService() {
        try {
            this.privateKey = readPrivateKey();
        } catch (Exception e) {
            throw new RuntimeException("Nie udało się załadować klucza prywatnego", e);
        }
    }

    public String generateToken(Users user) {
        Set<String> roles = new HashSet<>();
        roles.add("USER");
        long now = System.currentTimeMillis() / 1000;

        return Jwt.issuer(ISSUER)
                .subject(String.valueOf(user.getUserId()))
                .upn(user.getEmail())
                .claim("username", user.getUsername())
                .groups(roles)
                .issuedAt(now)
                .expiresAt(now + EXPIRATION_SECONDS)
                .sign(privateKey);
    }


    // ... reszta Twojej metody readPrivateKey bez zmian ...
    private PrivateKey readPrivateKey() throws Exception {
        InputStream is;
        is = Thread.currentThread().getContextClassLoader().getResourceAsStream(TokenService.PRIVATE_KEY_LOCATION.substring(1));
        if (is == null) {
            is = TokenService.class.getResourceAsStream(TokenService.PRIVATE_KEY_LOCATION);
        }
        if (is == null) {
            throw new RuntimeException("Nie znaleziono pliku klucza prywatnego: " + TokenService.PRIVATE_KEY_LOCATION);
        }
        String key = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        String privateKeyPEM = key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decodedKey = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decodedKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }
}