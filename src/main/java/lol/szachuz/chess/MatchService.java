package lol.szachuz.chess;

import com.github.bhlangonijr.chesslib.Side;
import lol.szachuz.chess.player.Player;

import lol.szachuz.email.EmailService;
import lol.szachuz.db.Repository.EmailRepository;

import lol.szachuz.chess.player.ai.AiPlayer;

import java.util.List;
import java.util.UUID;

import java.util.concurrent.CompletableFuture;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service operating matches.
 * It's a singleton.
 * @author Rafał Kubacki
 */
public final class MatchService {
    private static final MatchService INSTANCE = new MatchService();
    private final InMemoryGameRepository repository = new InMemoryGameRepository();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    //private final List<MatchEventListener> listeners = new CopyOnWriteArrayList<>();

    private MatchService() {
        scheduler.scheduleAtFixedRate(this::checkClocks, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * Method returning a reference to the instance of the {@link MatchService}.
     * @return {@link MatchService} refenrence.
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

        CompletableFuture.runAsync(() -> {
            try {
                EmailRepository emailRepo = new EmailRepository();
                EmailService emailService = new EmailService();

                int id1 = (int) p1.getId();
                int id2 = (int) p2.getId();

                String email1 = emailRepo.getEmailByUserId(id1);
                String email2 = emailRepo.getEmailByUserId(id2);
                String name1 = emailRepo.getUsernameByUserId(id1);
                String name2 = emailRepo.getUsernameByUserId(id2);

                if (email1 != null) {
                    emailService.sendGameStartEmail(email1, name2, match.getMatchUUID());
                }

                if (email2 != null) {
                    emailService.sendGameStartEmail(email2, name1, match.getMatchUUID());
                }

            } catch (Exception e) {
                System.err.println("Błąd w procesie wysyłania maili: " + e.getMessage());
                e.printStackTrace();
            }
        });


        return match;
    }

    /**
     * Method that tries to process and apply move request sent by player
     * @param playerId {@code long} ID of a player that wants to move
     * @param move {@link MoveMessage} record with move data.
     * @throws IllegalStateException if player tried to make an illegal move or match doesn't exist.
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
     */
    public MoveResult forfeit(long playerId) {
        Match match = loadMatchByPlayerId(playerId);

        if (match == null) {
            throw new IllegalStateException("No active match");
        }

        match.forfeit(playerId);

        repository.remove(match);

        return new MoveResult(match.getFen(), GameStatus.FORFEIT, match.getResult(), null, -1, -1);
    }

    /**
     * Finds a match what has a specific player in it.
     * @param playerId {@code long} ID of a player we're looking for.
     * @return Match with that player. Can be either {@link Match} or {@code null}.
     */
    public Match loadMatchByPlayerId(long playerId) {
        return repository.findByPlayer(playerId);
    }

    /**
     * Finds a match basing on it's UUID.
     * @param matchId {@code String} UUID of a match we're looking for.
     * @return Match with that UUID. Can be either {@link Match} or {@code null}.
     */
    public Match loadMatchByMatchId(String matchId) {
        return repository.findById(matchId);
    }

    /**
     * Method privided for scheduler.
     */
    private void checkClocks() {
        for (Match match : repository.findAll()) {
            if (match.getStatus() != GameStatus.ACTIVE) continue;

            Side side = match.getSideToMove();
            match.consumeTimeForSide(side);

            if (match.getWhiteTimeRemaining() <= 0) {
                match.timeoutWhite();
            } else if (match.getBlackTimeRemaining() <= 0) {
                match.timeoutBlack();
            } else {
                String timeTick =
                        "{ \"type\": \"TIME_TICK\",  \"timeRemaining\": { \"white\": \"" + match.getWhiteTimeRemaining()
                        + "\", \"black\": \"" + match.getBlackTimeRemaining() + "\"}}";
                ChessSocketRegistry.broadcast(match.getMatchUUID(), timeTick);
                continue;
            }

            repository.remove(match);
            MoveResult result = new MoveResult(
                match.getFen(),
                match.getStatus(),
                match.getResult(),
                match.getSideToMove(),
                match.getWhiteTimeRemaining(),
                match.getBlackTimeRemaining()
            );
            ChessSocketRegistry.broadcast(match.getMatchUUID(), result.toJson());
        }
    }
}
