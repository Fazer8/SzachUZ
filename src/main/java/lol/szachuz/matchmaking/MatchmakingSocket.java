package lol.szachuz.matchmaking;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lol.szachuz.db.Entities.Leaderboard;
import lol.szachuz.db.Repository.LeaderboardRepository;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@ServerEndpoint("/ws/matchmaking")
public class MatchmakingSocket {

    // Tworzymy repozytorium ręcznie, aby uniknąć problemów z wstrzykiwaniem w WebSocket
    private final LeaderboardRepository leaderboardRepository = new LeaderboardRepository();
    private final MatchmakingService matchmakingService = MatchmakingService.getInstance();

    private int userId;

    @OnOpen
    public void onOpen(Session session) {
        Map<String, List<String>> params = session.getRequestParameterMap();
        List<String> tokenList = params.get("token");

        if (tokenList == null || tokenList.isEmpty()) {
            System.out.println("Brak tokena - zamykam.");
            close(session);
            return;
        }

        try {
            // 1. Parsowanie ID z tokena
            this.userId = parseUserIdFromToken(tokenList.get(0));

            // 2. Pobieranie danych z bazy (MMR + Nick)
            int mmr = 1200;
            String username = "Gracz " + userId; // Domyślna nazwa

            try {
                Leaderboard lb = leaderboardRepository.findByUserId(userId);
                if (lb != null) {
                    mmr = lb.getMmr();
                    if (lb.getUser() != null) {
                        username = lb.getUser().getUsername();
                    }
                }
            } catch (Exception dbEx) {
                System.err.println("Błąd pobierania danych z bazy: " + dbEx.getMessage());
            }

            System.out.println("Połączono: " + username + " [" + userId + "]");

            // 3. Dodanie do kolejki z pobranym nickiem
            matchmakingService.addPlayerToQueue(new QueuedPlayer(userId, mmr, username, session));

        } catch (Exception e) {
            System.err.println("Błąd w onOpen: " + e.getMessage());
            e.printStackTrace();
            close(session);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        if (message == null) return;

        try {
            if (message.startsWith("ACCEPT:")) {
                String matchId = message.split(":")[1];
                matchmakingService.handleAccept(matchId, this.userId);
            } else if (message.startsWith("DECLINE:")) {
                String matchId = message.split(":")[1];
                matchmakingService.handleReject(matchId, this.userId);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        if (!(t instanceof java.io.EOFException)) {
            System.err.println("Błąd WebSocket: " + t.getMessage());
        }
    }

    private void close(Session s) {
        try { s.close(); } catch (Exception e) {}
    }

    private int parseUserIdFromToken(String token) {
        String[] chunks = token.split("\\.");
        if (chunks.length < 2) throw new RuntimeException("Invalid JWT format");

        Base64.Decoder decoder = Base64.getUrlDecoder();
        String payloadJson = new String(decoder.decode(chunks[1]), StandardCharsets.UTF_8);

        String searchKey = "\"sub\"";
        int subIndex = payloadJson.indexOf(searchKey);

        if (subIndex != -1) {
            int startQuote = payloadJson.indexOf("\"", subIndex + searchKey.length());
            while (payloadJson.charAt(startQuote) != '"') startQuote++;
            int endQuote = payloadJson.indexOf("\"", startQuote + 1);
            if (endQuote != -1) {
                return Integer.parseInt(payloadJson.substring(startQuote + 1, endQuote));
            }
        }
        throw new RuntimeException("Token missing 'sub' claim");
    }
}