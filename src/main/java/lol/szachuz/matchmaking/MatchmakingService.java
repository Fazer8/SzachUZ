package lol.szachuz.matchmaking;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*; // <--- WAŻNE IMPORTY

public class MatchmakingService {

    private static MatchmakingService instance;

    // "Serce" matchmakingu - wątek wykonujący zadania cykliczne
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private MatchmakingService() {
        // URUCHAMIAMY PĘTLĘ: Co 1 sekundę uruchom findMatch()
        // Dzięku temu system ciągle sprawdza, czy czas oczekiwania już pozwala na połączenie graczy
        scheduler.scheduleAtFixedRate(this::findMatch, 1, 1, TimeUnit.SECONDS);
    }

    public static synchronized MatchmakingService getInstance() {
        if (instance == null) {
            instance = new MatchmakingService();
        }
        return instance;
    }

    private final Queue<QueuedPlayer> queue = new ConcurrentLinkedQueue<>();
    private final Map<String, PendingMatch> pendingMatches = new ConcurrentHashMap<>();

    public void addPlayerToQueue(QueuedPlayer player) {
        queue.add(player);
        System.out.println("Gracz dodany do kolejki (MMR: " + player.getMmr() + "). Liczba graczy: " + queue.size());
        // findMatch() wywoła się samo za chwilę dzięki schedulerowi, ale możemy też wywołać ręcznie dla szybkości
        findMatch();
    }

    public void removePlayer(int userId) {
        boolean removedFromQueue = queue.removeIf(p -> p.getUserId() == userId);

        if (removedFromQueue) {
            System.out.println("Gracz usunięty z kolejki ID: " + userId);
        } else {
            String matchIdToRemove = null;
            for (PendingMatch pm : pendingMatches.values()) {
                if (pm.getPlayer1().getUserId() == userId || pm.getPlayer2().getUserId() == userId) {
                    matchIdToRemove = pm.getMatchId();
                    break;
                }
            }

            if (matchIdToRemove != null) {
                System.out.println("Gracz rozłączył się podczas akceptacji. Traktujemy jako ODRZUCENIE.");
                handleReject(matchIdToRemove, userId);
            }
        }
    }

    // ==========================================
    // 1. ALGORYTM WYSZUKIWANIA
    // ==========================================
    public synchronized void findMatch() {
        // Jeśli w kolejce jest mniej niż 2 graczy, szkoda czasu na pętlę
        if (queue.size() < 2) return;

        // DEBUG: Możesz odkomentować, żeby widzieć, że pętla żyje
        // System.out.println("Scheduler sprawdza kolejkę... Graczy: " + queue.size());

        Iterator<QueuedPlayer> iterator = queue.iterator();
        while (iterator.hasNext()) {
            QueuedPlayer p1 = iterator.next();

            // Czyścimy martwe sesje
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
                    return; // Znaleziono parę, kończymy ten cykl
                }
            }
        }
    }

    private boolean isGoodMatch(QueuedPlayer p1, QueuedPlayer p2) {
        long waitTime = System.currentTimeMillis() - p1.getJoinTime();
        long waitTimeSeconds = waitTime / 1000;

        // =========================================================
        // USTAWIENIA (Możesz tu zmienić parametry szybkości)
        // =========================================================

        // Co 5 sekund zakres rośnie o 10 MMR
        // Czyli: 1 minuta czekania = poszerzenie o 120 MMR
        int expansion = (int) (waitTimeSeconds / 5) * 10;

        int allowedDiff = 30 + expansion;
        // =========================================================

        int actualDiff = Math.abs(p1.getMmr() - p2.getMmr());

        // Logujemy postępy co jakiś czas (np. co 10 sekund), żeby nie spamować konsoli
        if (waitTimeSeconds % 10 == 0) {
            // System.out.println("Sprawdzam: " + p1.getUserId() + " vs " + p2.getUserId() + " | Diff: " + actualDiff + " | Allowed: " + allowedDiff);
        }

        return actualDiff <= allowedDiff;
    }

    // ==========================================
    // 2. OBSŁUGA AKCEPTACJI
    // ==========================================

    private void createPendingMatch(QueuedPlayer p1, QueuedPlayer p2) {
        String matchId = "match_" + System.currentTimeMillis() + "_" + p1.getUserId();
        PendingMatch pending = new PendingMatch(matchId, p1, p2);
        pendingMatches.put(matchId, pending);

        System.out.println("Propozycja meczu: " + matchId + " (" + p1.getMmr() + " vs " + p2.getMmr() + ")");
        sendJson(p1, "MATCH_PROPOSED", matchId, null);
        sendJson(p2, "MATCH_PROPOSED", matchId, null);
    }

    public void handleAccept(String matchId, int userId) {
        PendingMatch match = pendingMatches.get(matchId);
        if (match == null) return;

        match.accept(userId);
        System.out.println("Gracz " + userId + " zaakceptował mecz " + matchId);

        if (match.isFullyAccepted()) {
            startGame(match);
            pendingMatches.remove(matchId);
        }
    }

    public void handleReject(String matchId, int rejectorId) {
        PendingMatch match = pendingMatches.remove(matchId);
        if (match == null) return;

        System.out.println("Mecz " + matchId + " odrzucony przez " + rejectorId);

        QueuedPlayer opponent = match.getOpponent(rejectorId);
        QueuedPlayer rejector = (opponent == match.getPlayer1()) ? match.getPlayer2() : match.getPlayer1();

        sendJson(rejector, "MATCH_CANCELLED", matchId, null);

        if (opponent != null && opponent.getSession().isOpen()) {
            System.out.println("Przywracam gracza " + opponent.getUserId() + " do kolejki.");
            queue.add(opponent);
            sendJson(opponent, "OPPONENT_DECLINED", matchId, null);
            // Scheduler i tak zaraz zadziała, więc nie musimy ręcznie wołać findMatch
        }
    }

    private void startGame(PendingMatch match) {
        sendJson(match.getPlayer1(), "GAME_START", match.getMatchId(), "WHITE");
        sendJson(match.getPlayer2(), "GAME_START", match.getMatchId(), "BLACK");
        System.out.println("Gra wystartowała: " + match.getMatchId());
    }

    private void sendJson(QueuedPlayer p, String type, String matchId, String color) {
        try {
            if (p.getSession() != null && p.getSession().isOpen()) {
                String json = String.format("{\"type\":\"%s\", \"matchId\":\"%s\"", type, matchId);
                if (color != null) {
                    json += ", \"color\":\"" + color + "\"";
                }
                json += "}";
                synchronized (p.getSession()) {
                    p.getSession().getBasicRemote().sendText(json);
                }
            }
        } catch (IllegalStateException | IOException e) {
            // Ignorujemy błędy przy zamykaniu
        }
    }
}