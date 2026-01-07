package lol.szachuz.matchmaking;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import lol.szachuz.chess.MatchService;
import lol.szachuz.chess.Match;
import lol.szachuz.chess.HumanPlayer;

public class MatchmakingService {

    private static MatchmakingService instance;

    // Scheduler na jednym wątku jest OK, bo operacje są błyskawiczne (tylko matematyka na liczbach)
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private MatchmakingService() {
        scheduler.scheduleAtFixedRate(this::findMatch, 1, 1, TimeUnit.SECONDS);
    }

    public static synchronized MatchmakingService getInstance() {
        if (instance == null) {
            instance = new MatchmakingService();
        }
        return instance;
    }

    // Kolejki Concurrent są kluczowe dla wielowątkowości WebSocketów
    private final Queue<QueuedPlayer> queue = new ConcurrentLinkedQueue<>();
    private final Map<String, PendingMatch> pendingMatches = new ConcurrentHashMap<>();

    public void addPlayerToQueue(QueuedPlayer player) {
        // 1. OCHRONA PRZED DUPLIKATAMI
        // Jeśli ten gracz już jest w kolejce (np. przez odświeżenie strony), usuń go najpierw
        removePlayer(player.getUserId());

        // 2. Dodaj nową sesję
        queue.add(player);

        System.out.println("Gracz " + player.getUserId() + " dołączył (MMR: " + player.getMmr() + "). W kolejce: " + queue.size());

        // 3. Szukaj meczu
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
        // Czas oczekiwania gracza, który czeka dłużej (p1)
        long waitTime = (System.currentTimeMillis() - p1.getJoinTime()) / 1000;

        // BAZA: Na start pozwalamy tylko na 20 punktów różnicy
        int baseDiff = 20;

        // ROZSZERZANIE: Co 5 sekund dodajemy 20 punktów zakresu
        // 0-5 sek: +/- 20
        // 5-10 sek: +/- 40
        // 10-15 sek: +/- 60
        // itd.
        int expansion = (int) (waitTime / 5) * 20;

        int allowedDiff = baseDiff + expansion;

        int actualDiff = Math.abs(p1.getMmr() - p2.getMmr());

        // Opcjonalny DEBUG, żebyś widział w konsoli jak algorytm myśli:
        // System.out.println("Porównanie: " + actualDiff + " vs Limit: " + allowedDiff + " (Czas: " + waitTime + "s)");

        return actualDiff <= allowedDiff;
    }

    private void createPendingMatch(QueuedPlayer p1, QueuedPlayer p2) {
        String matchId = "match_" + System.currentTimeMillis() + "_" + p1.getUserId();
        PendingMatch pending = new PendingMatch(matchId, p1, p2);
        pendingMatches.put(matchId, pending);

        System.out.println("Znaleziono parę: " + matchId);
        sendJson(p1, "MATCH_PROPOSED", matchId, null);
        sendJson(p2, "MATCH_PROPOSED", matchId, null);
    }

    public void handleAccept(String matchId, int userId) {
        PendingMatch match = pendingMatches.get(matchId);
        if (match == null) return;

        match.accept(userId);
        System.out.println("Gracz " + userId + " zaakceptował.");

        if (match.isFullyAccepted()) {
            startGame(match);
            pendingMatches.remove(matchId);
        }
    }

    public void handleReject(String matchId, int rejectorId) {
        PendingMatch match = pendingMatches.remove(matchId);
        if (match == null) return;

        sendJson(match.getOpponent(rejectorId), "MATCH_CANCELLED", matchId, null);

        QueuedPlayer opponent = match.getOpponent(rejectorId);
        if (opponent != null && opponent.getSession().isOpen()) {
            queue.add(opponent);
            sendJson(opponent, "OPPONENT_DECLINED", matchId, null);
        }
    }

    // Tu zostawiamy Twoją logikę (bez Servletu, bezpośrednie tworzenie)
    private void startGame(PendingMatch match) {
        try {
            long p1Id = match.getPlayer1().getUserId();
            long p2Id = match.getPlayer2().getUserId();

            HumanPlayer white = new HumanPlayer(p1Id);
            HumanPlayer black = new HumanPlayer(p2Id);

            Match game = MatchService.getInstance().createMatch(white, black);
            String gameUUID = game.getMatchUUID();

            System.out.println("Gra START! UUID: " + gameUUID);

            sendJson(match.getPlayer1(), "GAME_START", gameUUID, "WHITE");
            sendJson(match.getPlayer2(), "GAME_START", gameUUID, "BLACK");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendJson(QueuedPlayer p, String type, String matchId, String color) {
        try {
            if (p.getSession() != null && p.getSession().isOpen()) {
                String json = String.format("{\"type\":\"%s\", \"matchId\":\"%s\"", type, matchId);
                if (color != null) json += ", \"color\":\"" + color + "\"";
                json += "}";
                synchronized (p.getSession()) {
                    p.getSession().getBasicRemote().sendText(json);
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}