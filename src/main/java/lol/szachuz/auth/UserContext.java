package lol.szachuz.auth;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

@RequestScoped
public class UserContext {

    @Context
    private HttpHeaders headers;

    // Cache'ujemy ID, żeby nie parsować tokena wielokrotnie w ramach jednego zapytania
    private Integer cachedUserId;

    public int getCurrentUserId() {
        if (cachedUserId != null) {
            return cachedUserId;
        }

        String authHeader = headers.getHeaderString("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new WebApplicationException("Missing or invalid Authorization header", Response.Status.UNAUTHORIZED);
        }

        try {
            String token = authHeader.substring(7);

            // Tutaj przenosimy twoją logikę parsowania
            cachedUserId = parseUserIdFromToken(token);
            return cachedUserId;
        } catch (Exception e) {
            throw new WebApplicationException("Invalid token", Response.Status.UNAUTHORIZED);
        }
    }

    private int parseUserIdFromToken(String token) {
        // TWOJA LOGIKA (przeniesiona z ProfileResource)
        String[] chunks = token.split("\\.");
        if (chunks.length < 2) {
            throw new RuntimeException("Invalid JWT format");
        }
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String payloadJson = new String(decoder.decode(chunks[1]), StandardCharsets.UTF_8);

        String searchKey = "\"sub\"";
        int subIndex = payloadJson.indexOf(searchKey);

        if (subIndex != -1) {
            int startQuote = payloadJson.indexOf("\"", subIndex + searchKey.length());
            while (payloadJson.charAt(startQuote) != '"') {
                startQuote++;
            }
            int endQuote = payloadJson.indexOf("\"", startQuote + 1);

            if (endQuote != -1) {
                String subValue = payloadJson.substring(startQuote + 1, endQuote);
                return Integer.parseInt(subValue);
            }
        }
        throw new RuntimeException("Token missing 'sub' claim");
    }
}