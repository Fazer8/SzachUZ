package lol.szachuz.matchmaking;

import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lol.szachuz.db.Entities.Leaderboard;
import lol.szachuz.db.Repository.LeaderboardRepository; // Twoje repozytorium

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@ServerEndpoint("/ws/matchmaking")
public class MatchmakingSocket {

    // ZMIANA: Zamiast @Inject, tworzymy to ręcznie.
    // W WebSocketach @Inject często nie działa, a Twoje repozytorium jest bezpieczne do użycia tak:
    private final LeaderboardRepository leaderboardRepository = new LeaderboardRepository();

    private final MatchmakingService matchmakingService = MatchmakingService.getInstance();
    private int userId;

    @OnOpen
    public void onOpen(Session session) {
        // 1. Pobieramy token z parametru URL (?token=...)
        // WebSockety nie mają nagłówków Authorization dostępnych tak łatwo jak REST
        Map<String, List<String>> params = session.getRequestParameterMap();
        List<String> tokenList = params.get("token");

        if (tokenList == null || tokenList.isEmpty()) {
            System.out.println("Brak tokena w URL - zamykam połączenie.");
            close(session);
            return;
        }

        String token = tokenList.get(0);

        try {
            // 2. RĘCZNE PARSOWANIE TOKENA (Logika z UserContext, ale lokalnie)
            // Dzięki temu omijamy problem z @RequestScoped w WebSocketach
            this.userId = parseUserIdFromToken(token);

            System.out.println("Gracz połączony: " + userId);

            // 3. Pobranie MMR z bazy (bezpieczne)
            int mmr = 1200; // Domyślna wartość
            if (leaderboardRepository != null) {
                try {
                    Leaderboard lb = leaderboardRepository.findByUserId(userId);
                    if (lb != null) {
                        mmr = lb.getMmr();
                    } else {
                        // Opcjonalnie: Jeśli gracza nie ma w tabeli, można go tu dodać, ale na razie tylko logujemy
                        System.out.println("Gracz " + userId + " nie ma wpisu w rankingu. Ustawiam 1200.");
                    }
                } catch (Exception dbEx) {
                    System.err.println("Błąd bazy danych (MMR): " + dbEx.getMessage());
                    // Nie przerywamy! Gracz zagra z domyślnym MMR.
                }
                // Wewnątrz metody onOpen, po pobraniu MMR:
                System.out.println("DEBUG: Gracz " + userId + " wchodzi z MMR: " + mmr);
                matchmakingService.addPlayerToQueue(new QueuedPlayer(userId, mmr, session));
            }

            // 4. Dodanie do kolejki
            matchmakingService.addPlayerToQueue(new QueuedPlayer(userId, mmr, session));

        } catch (Exception e) {
            System.err.println("Błąd autoryzacji/połączenia: " + e.getMessage());
            e.printStackTrace();
            close(session);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        if (message == null) return;

        if (message.startsWith("ACCEPT:")) {
            try {
                String matchId = message.split(":")[1];
                matchmakingService.handleAccept(matchId, this.userId);
            } catch (Exception e) { e.printStackTrace(); }
        }
        else if (message.startsWith("DECLINE:")) {
            try {
                String matchId = message.split(":")[1];
                matchmakingService.handleReject(matchId, this.userId);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @OnClose
    public void onClose(Session session) {
        if (userId != 0) {
            matchmakingService.removePlayer(this.userId);
        }
    }

    @OnError
    public void onError(Session session, Throwable t) {
        // Ignorujemy błędy EOF (nagłe zamknięcie karty przez usera)
        if (!(t instanceof java.io.EOFException)) {
            System.err.println("Błąd WebSocket: " + t.getMessage());
        }
    }

    private void close(Session s) {
        try { s.close(); } catch (Exception e) {}
    }

    // --- LOGIKA WYCIĄGNIĘTA Z UserContext ---
    // Musi być tutaj, bo w WebSockecie nie mamy dostępu do UserContext (RequestScoped)
    private int parseUserIdFromToken(String token) {
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