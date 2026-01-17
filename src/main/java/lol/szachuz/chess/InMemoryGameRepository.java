package lol.szachuz.chess;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class InMemoryGameRepository {

    private final Map<String, Match> matches = new ConcurrentHashMap<>();
    private final Map<Long, String> playerToMatch = new ConcurrentHashMap<>();

    public void save(Match match) {
        matches.put(match.getMatchUUID(), match);
        playerToMatch.put(match.getWhite().getId(), match.getMatchUUID());
        playerToMatch.put(match.getBlack().getId(), match.getMatchUUID());
    }

    public Match findByPlayer(long playerId) {
        String matchId = playerToMatch.get(playerId);
        return matchId == null ? null : matches.get(matchId);
    }

    public Match findById(String matchId) {
        return matches.get(matchId);
    }

    public void remove(Match match) {
        matches.remove(match.getMatchUUID());
        playerToMatch.remove(match.getWhite().getId());
        playerToMatch.remove(match.getBlack().getId());
    }

    public boolean isPlayerInGame(long playerId) {
        return playerToMatch.containsKey(playerId);
    }
    // --- NOWA METODA ---
    public void archive(Match match) {
        // 1. Usuwamy tylko powiązanie graczy, żeby mogli zagrać nowy mecz
        playerToMatch.remove(match.getWhite().getId());
        playerToMatch.remove(match.getBlack().getId());

        // 2. Mecz zostaje w 'matches', więc PDF zadziała.

        // 3. (Opcjonalnie) Usuń mecz całkowicie po 10 minutach, żeby nie zapchać RAMu
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            matches.remove(match.getMatchUUID());
        }, 10, TimeUnit.MINUTES);
    }
}