package lol.szachuz.auth;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@RequestScoped
public class UserContext {
    /**
     * Represents HTTP headers associated with the current request context.
     *
     * This field is automatically injected by the framework using the {@code @Context} annotation.
     * It provides access to HTTP header information, such as the Authorization header,
     * which may be used for authentication or other purposes.
     */

    @Context
    private HttpHeaders headers;

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

            cachedUserId = parseUserIdFromToken(token);
            return cachedUserId;
        } catch (Exception e) {
            throw new WebApplicationException("Invalid token", Response.Status.UNAUTHORIZED);
        }
    }

    private int parseUserIdFromToken(String token) {
        return JWTDecoder.parseUserIdFromToken(token);
    }
}