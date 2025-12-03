package lol.szachuz.auth;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Ten filtr przechwytuje żądania i sprawdza, czy istnieje ciasteczko 'authToken'.
 * Jeśli tak, jego wartość jest kopiowana do nagłówka 'Authorization: Bearer',
 * aby domyślny MicroProfile JWT-Auth Filter mógł go przetworzyć.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class CookieToHeaderFilter implements ContainerRequestFilter {

    private static final String AUTH_COOKIE_NAME = "authToken";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // 1. Sprawdzenie, czy ciasteczko 'authToken' istnieje w żądaniu
        Cookie authCookie = requestContext.getCookies().get(AUTH_COOKIE_NAME);

        if (authCookie != null) {
            String token = authCookie.getValue();

            // 2. Wstawienie tokenu do nagłówka Authorization (w formacie Bearer)
            // Moduł JWT Auth (np. MicroProfile) szuka tokenu właśnie w tym nagłówku.
            requestContext.getHeaders().add(AUTHORIZATION_HEADER, BEARER_PREFIX + token);

            // Logowanie pomocnicze: można to usunąć po debugowaniu
            System.out.println("JWT Filter: Wstawiono token z ciasteczka " + AUTH_COOKIE_NAME + " do nagłówka Authorization.");
        } else {
            // Logowanie pomocnicze
            System.out.println("JWT Filter: Brak ciasteczka " + AUTH_COOKIE_NAME + " w żądaniu.");
        }
    }
}