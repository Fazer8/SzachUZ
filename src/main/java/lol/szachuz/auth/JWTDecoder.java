package lol.szachuz.auth;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Class with a JWT decoder.
 * @author Marcin Dudyński
 */
public class JWTDecoder {
    /**
     * Decodes the JWT.
     * @param token token to decode.
     * @return user's ID.
     */
    public static int parseUserIdFromToken(String token) {
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
