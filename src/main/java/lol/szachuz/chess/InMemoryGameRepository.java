package lol.szachuz.chess;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
}