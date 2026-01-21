package lol.szachuz.chess;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository with all the ongoing matches, stored in app's runtime memory.
 * @author Rafał Kubacki
 */
public final class InMemoryGameRepository {

    private final Map<String, Match> matches = new ConcurrentHashMap<>();
    private final Map<Long, String> playerToMatch = new ConcurrentHashMap<>();

    /**
     * Adds a match to the repository.
     * @param match a {@link Match} object to save.
     */
    public void save(Match match) {
        matches.put(match.getMatchUUID(), match);
        playerToMatch.put(match.getWhite().getId(), match.getMatchUUID());
        playerToMatch.put(match.getBlack().getId(), match.getMatchUUID());
    }

    /**
     * Returns all matches.
     * @return all matches.
     */
    public Collection<Match> findAll() {
        return matches.values();
    }
    /**
     * Finds a match what has a specific player in it.
     * @param playerId {@code long} ID of a player we're looking for.
     * @return Match with that player. Can be either {@link Match} or {@code null}.
     */
    public Match findByPlayer(long playerId) {
        String matchId = playerToMatch.get(playerId);
        return matchId == null ? null : matches.get(matchId);
    }

    /**
     * Finds a match basing on it's UUID.
     * @param matchId {@code String} UUID of a match we're looking for.
     * @return Match with that UUID. Can be either {@link Match} or {@code null}.
     */
    public Match findById(String matchId) {
        return matches.get(matchId);
    }

    /**
     * Removes a match from the registry.
     * @param match a {@link Match} to be removed.
     */
    public void remove(Match match) {
        matches.remove(match.getMatchUUID());
        playerToMatch.remove(match.getWhite().getId());
        playerToMatch.remove(match.getBlack().getId());
    }

    /**
     * Checks if a player is in game.
     * @param playerId {@code long} ID of a player we want to check.
     * @return an anwser to this question.
     */
    public boolean isPlayerInGame(long playerId) {
        return playerToMatch.containsKey(playerId);
    }
}