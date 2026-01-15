package lol.szachuz.matchmaking;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lol.szachuz.SzachuzWebSocket;
import lol.szachuz.auth.JWTDecoder;
import lol.szachuz.db.Entities.Leaderboard;
import lol.szachuz.db.Repository.LeaderboardRepository;

import java.util.List;
import java.util.Map;

@ServerEndpoint("/ws/matchmaking")
public class MatchmakingSocket implements SzachuzWebSocket {

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
            this.userId = JWTDecoder.parseUserIdFromToken(tokenList.getFirst());

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
            sendError(e.getMessage(), session);
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
}