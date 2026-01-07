package lol.szachuz.matchmaking;


import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lol.szachuz.auth.UserContext;
import lol.szachuz.db.Entities.Leaderboard;
import lol.szachuz.db.Repository.LeaderboardRepository;

import java.util.List;
import java.util.Map;

@ServerEndpoint("/ws/matchmaking")
public class MatchmakingSocket {
    @Inject
    private UserContext userContext;
    LeaderboardRepository leaderboardRepository;

    private final MatchmakingService matchmakingService = MatchmakingService.getInstance();

    private int userId;

    @OnOpen
    public void onOpen(Session session) {
        Map<String, List<String>> params = session.getRequestParameterMap();
        List<String> tokenList = params.get("token");

        if (tokenList == null || tokenList.isEmpty()) {
            System.out.println("Brak tokena - zamykam");
            close(session);
            return;
        }

        try {
            this.userId = userContext.getCurrentUserId();
            System.out.println("Gracz połączony: " + userId);

            // --- ZMIANA: POBIERANIE MMR Z BAZY ---
            Leaderboard leaderboard = leaderboardRepository.findByUserId(userId);
            int mmr = leaderboard.getMmr();
            // -------------------------------------

            matchmakingService.addPlayerToQueue(new QueuedPlayer(userId, mmr, session));

        } catch (Exception e) {
            System.err.println("Błąd autoryzacji: " + e.getMessage());
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (message.startsWith("DECLINE:")) {
            try {
                String matchId = message.split(":")[1];
                matchmakingService.handleReject(matchId, this.userId);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        // Ignorujemy błędy zamknięcia połączenia, żeby nie śmiecić w logach
        if (!(t instanceof java.io.EOFException)) {
            System.err.println("Błąd WebSocket: " + t.getMessage());
        }
        if (userId != 0) {
            matchmakingService.removePlayer(this.userId);
        }
    }

    private void close(Session s) {
        try { s.close(); } catch (Exception e) {}
    }
}