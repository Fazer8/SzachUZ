package lol.szachuz.chess;

import java.util.UUID;

public class MatchService {

    private static final MatchService INSTANCE = new MatchService();

    private final InMemoryGameRepository repository = new InMemoryGameRepository();

    public static MatchService getInstance() {
        return INSTANCE;
    }

    public Match createMatch(Player p1, Player p2) {

        if (repository.isPlayerInGame(p1.getId()) ||
                repository.isPlayerInGame(p2.getId())) {
            throw new IllegalStateException("Player already in game");
        }

        Match match = new Match(
                UUID.randomUUID().toString(),
                p1,
                p2
        );

        repository.save(match);
        return match;
    }
    public MoveResult processMove(long playerId, String from, String to) {
        Match match = repository.findByPlayer(playerId);

        if (match == null) {
            throw new IllegalStateException("No active match");
        }

        match.applyMove(playerId, from, to);
        if (match.getStatus() == GameStatus.FINISHED) {
            repository.remove(match);
        }

        if (match.isOver()) {
            repository.remove(match);
        }

        return MoveResult.from(match);
    }
    public Match loadMatch(long playerId) {
        return repository.findByPlayer(playerId);
    }
}