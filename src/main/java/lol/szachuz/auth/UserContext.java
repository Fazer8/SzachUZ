package lol.szachuz.auth;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

/**
 * A request-scoped class that provides user-related context within the
 * HTTP request lifecycle. This class handles the retrieval of user
 * information, such as the user ID, based on the Authorization header
 * in HTTP requests.
 */
@RequestScoped
public class UserContext {

    @Context
    private HttpHeaders headers;

    private Integer cachedUserId;

    /**
     * Returns user's ID
     * @return user's ID
     */
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

    /**
     * Retrieves ID from token
     * @param token to retreive ID from
     * @return ID
     */
    private int parseUserIdFromToken(String token) {
        return JWTDecoder.parseUserIdFromToken(token);
    }
}