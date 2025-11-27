package lol.szachuz.auth;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import org.eclipse.microprofile.jwt.JsonWebToken;
import io.smallrye.jwt.auth.principal.DefaultJWTParser;

@RequestScoped
public class JwtProducer {

    @Inject
    private HttpServletRequest request;

    @Produces
    @RequestScoped
    public JsonWebToken produceJwt() {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring("Bearer ".length());

        try {
            DefaultJWTParser parser = new DefaultJWTParser();
            return parser.parse(token);
        } catch (Exception e) {
            throw new RuntimeException("Nie udało się sparsować JWT", e);
        }
    }
}
