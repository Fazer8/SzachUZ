package lol.szachuz.chess;

import lol.szachuz.chess.player.Player;
import lol.szachuz.chess.player.ai.AiPlayer;

import java.util.UUID;

/**
 * Service operating matches.
 * It's a singleton.
 * @author Rafał Kubacki
 */
public final class MatchService {

    private static final MatchService INSTANCE = new MatchService();

    private final InMemoryGameRepository repository = new InMemoryGameRepository();

    /**
     * Method returning a reference to the instance of the {@link MatchService}.
     * @return {@link MatchService} refenrence.
     * @author Rafał Kubacki
     */
    public static MatchService getInstance() {
        return INSTANCE;
    }

    /**
     * Method that creates a new match.
     * Player can be either {@link lol.szachuz.chess.player.HumanPlayer} or {@link lol.szachuz.chess.player.ai.AiPlayer},
     * but no more than one AiPlayer per match.
     * @param p1 one of the two players taking part in this match. Doesn't matter which one.
     * @param p2 other one the two players taking part in this match.
     * @return a new {@link Match} object.
     * @author Rafał Kubacki
     */
    public Match createMatch(Player p1, Player p2) {

        if (repository.isPlayerInGame(p1.getId()) ||
                repository.isPlayerInGame(p2.getId())) {
            throw new IllegalStateException("Player already in game");
        }

        if (p1 instanceof AiPlayer && p2 instanceof AiPlayer) {
            throw new IllegalStateException("Cannot create a game with two Ai Players!");
        }

        Match match = new Match(
                UUID.randomUUID().toString(),
                p1,
                p2
        );

        repository.save(match);
        return match;
    }

    /**
     * Method that tries to process and apply move request sent by player
     * @param playerId {@code long} ID of a player that wants to move
     * @param move {@link MoveMessage} record with move data.
     * @throws IllegalStateException if player tried to make an illegal move or match doesn't exist.
     * @author Rafał Kubacki
     */
    public MoveResult processMove(long playerId, MoveMessage move) {
        Match match = loadMatchByPlayerId(playerId);

        if (match == null) {
            throw new IllegalStateException("No active match");
        }

        match.applyMove(playerId, move.from(), move.to());
        if (match.getStatus() != GameStatus.ACTIVE || match.isOver()) {
            repository.remove(match);
        }

        return MoveResult.from(match);
    }

    /**
     * Method that forcefully ends match by forfeit.
     * @param playerId {@code long} ID of a player that forfeits.
     * @throws IllegalStateException if match doesnt exist.
     * @author Rafał Kubacki
     */
    public MoveResult forfeit(long playerId) {
        Match match = loadMatchByPlayerId(playerId);

        if (match == null) {
            throw new IllegalStateException("No active match");
        }

        match.forfeit(playerId);

        repository.remove(match);

        return new MoveResult(match.getFen(), GameStatus.FORFEIT, match.getResult(), null);
    }

    /**
     * Finds a match what has a specific player in it.
     * @param playerId {@code long} ID of a player we're looking for.
     * @return Match with that player. Can be either {@link Match} or {@code null}.
     * @author Rafał Kubacki
     */
    public Match loadMatchByPlayerId(long playerId) {
        return repository.findByPlayer(playerId);
    }

    /**
     * Finds a match basing on it's UUID.
     * @param matchId {@code String} UUID of a match we're looking for.
     * @return Match with that UUID. Can be either {@link Match} or {@code null}.
     * @author Rafał Kubacki
     */
    public Match loadMatchByMatchId(String matchId) {
        return repository.findById(matchId);
    }
}