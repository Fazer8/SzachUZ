package lol.szachuz.matchmaking;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import lol.szachuz.chess.MatchService;
import lol.szachuz.chess.Match;
import lol.szachuz.chess.player.HumanPlayer;

public class MatchmakingService {

    private static MatchmakingService instance;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private MatchmakingService() {
        scheduler.scheduleAtFixedRate(this::findMatch, 1, 1, TimeUnit.SECONDS);
    }

    public static synchronized MatchmakingService getInstance() {
        if (instance == null) instance = new MatchmakingService();
        return instance;
    }

    private final Queue<QueuedPlayer> queue = new ConcurrentLinkedQueue<>();
    private final Map<String, PendingMatch> pendingMatches = new ConcurrentHashMap<>();

    public void addPlayerToQueue(QueuedPlayer player) {
        removePlayer(player.getUserId());

        queue.add(player);
        System.out.println("Dodano do kolejki: " + player.getUsername() + " (" + player.getMmr() + ")");
        findMatch();
    }

    public void removePlayer(int userId) {
        queue.removeIf(p -> p.getUserId() == userId);
        pendingMatches.values().removeIf(pm -> {
            if (pm.getPlayer1().getUserId() == userId || pm.getPlayer2().getUserId() == userId) {
                handleReject(pm.getMatchId(), userId);
                return true;
            }
            return false;
        });
    }

    public synchronized void findMatch() {
        if (queue.size() < 2) return;

        Iterator<QueuedPlayer> iterator = queue.iterator();
        while (iterator.hasNext()) {
            QueuedPlayer p1 = iterator.next();

            if (p1.getSession() == null || !p1.getSession().isOpen()) {
                iterator.remove();
                continue;
            }

            for (QueuedPlayer p2 : queue) {
                if (p1.getUserId() == p2.getUserId()) continue;

                if (isGoodMatch(p1, p2)) {
                    createPendingMatch(p1, p2);
                    queue.remove(p1);
                    queue.remove(p2);
                    return;
                }
            }
        }
    }

    private boolean isGoodMatch(QueuedPlayer p1, QueuedPlayer p2) {
        long waitTime = (System.currentTimeMillis() - p1.getJoinTime()) / 1000;

        int expansion = (int) (waitTime / 5) * 20;
        int allowedDiff = 20 + expansion;
        return Math.abs(p1.getMmr() - p2.getMmr()) <= allowedDiff;
    }

    private void createPendingMatch(QueuedPlayer p1, QueuedPlayer p2) {
        String matchId = "match_" + System.currentTimeMillis() + "_" + p1.getUserId();
        PendingMatch pending = new PendingMatch(matchId, p1, p2);
        pendingMatches.put(matchId, pending);

        System.out.println("Mecz: " + p1.getUsername() + " vs " + p2.getUsername());

        sendJson(p1, "MATCH_PROPOSED", matchId, null, p2.getUsername());
        sendJson(p2, "MATCH_PROPOSED", matchId, null, p1.getUsername());
    }

    public void handleAccept(String matchId, int userId) {
        PendingMatch match = pendingMatches.get(matchId);
        if (match == null) return;

        match.accept(userId);

        if (match.isFullyAccepted()) {
            startGame(match);
            pendingMatches.remove(matchId);
        }
    }

    public void handleReject(String matchId, int rejectorId) {
        PendingMatch match = pendingMatches.remove(matchId);
        if (match == null) return;

        sendJson(match.getOpponent(rejectorId), "MATCH_CANCELLED", matchId, null, null);

        QueuedPlayer opponent = match.getOpponent(rejectorId);
        if (opponent != null && opponent.getSession().isOpen()) {
            queue.add(opponent);
            sendJson(opponent, "OPPONENT_DECLINED", matchId, null, null);
        }
    }

    private void startGame(PendingMatch match) {
        try {
            HumanPlayer white = new HumanPlayer(match.getPlayer1().getUserId());
            HumanPlayer black = new HumanPlayer(match.getPlayer2().getUserId());

            Match game = MatchService.getInstance().createMatch(white, black);
            String gameUUID = game.getMatchUUID();

            sendJson(match.getPlayer1(), "GAME_START", gameUUID, "WHITE", null);
            sendJson(match.getPlayer2(), "GAME_START", gameUUID, "BLACK", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendJson(QueuedPlayer p, String type, String matchId, String color, String opponentName) {
        try {
            if (p.getSession() != null && p.getSession().isOpen()) {
                StringBuilder json = new StringBuilder();
                json.append("{");
                json.append("\"type\":\"").append(type).append("\",");
                json.append("\"matchId\":\"").append(matchId).append("\"");

                if (color != null) {
                    json.append(",\"color\":\"").append(color).append("\"");
                }
                if (opponentName != null) {
                    json.append(",\"opponentName\":\"").append(opponentName).append("\"");
                }

                json.append("}");
                synchronized (p.getSession()) {
                    p.getSession().getBasicRemote().sendText(json.toString());
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}