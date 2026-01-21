package lol.szachuz.chess;

import com.github.bhlangonijr.chesslib.Side;
import lol.szachuz.chess.player.Player;
import lol.szachuz.chess.player.ai.AiPlayer;

import java.util.Timer;

/**
 * Klasa reprezentująca Sesję Gry (GameSession)
 * @author Rafał Kubacki
 */
public class Match {
    private final ChessEngine engine;
    private final Player white, black;
    private final String matchUUID;
    private GameStatus status = GameStatus.ACTIVE;

    private static final long MS_IN_SECONDS = 1000;
    private static final long SECONDS_IN_MINUTES = 60;
    private static final long MINUTES = 5;
    private static final long INITIAL_TIME_MS = MINUTES * SECONDS_IN_MINUTES * MS_IN_SECONDS;
    private long whiteTimeRemaining = INITIAL_TIME_MS;

    private long blackTimeRemaining = INITIAL_TIME_MS;
    private long lastMoveTimestamp;

    private GameResult gameResult;

    /**
     * Constructor of the {@link Match} object.
     * @param matchUUID {@code String} with a UUID what will be used to represent a match.
     * @param white Object representing a Player that will use white pieces
     *              (either {@link lol.szachuz.chess.player.HumanPlayer} or {@link lol.szachuz.chess.player.ai.AiPlayer}
     * @param black Object representing a Player that will use black pieces
     *              (either {@link lol.szachuz.chess.player.HumanPlayer} or {@link lol.szachuz.chess.player.ai.AiPlayer}
     */
    public Match(String matchUUID, Player white, Player black) {
        engine = new ChessEngine();
        this.black = black;
        this.white = white;
        this.matchUUID = matchUUID;
        this.gameResult = GameResult.ONGOING;
        this.lastMoveTimestamp = System.currentTimeMillis();
    }

    /**
     * Method that tries to apply move request sent by player
     * @param playerId {@code long} ID of a player that wants to move
     * @param from {@code String} which piece to move, in FEN notation.
     * @param to {@code String} where to move that piece, in FEN notation.
     * @throws IllegalStateException if player tried to make an illegal move.
     */
    public synchronized void applyMove(long playerId, String from, String to) {
        if (status != GameStatus.ACTIVE) {
            throw new IllegalStateException("Game already finished");
        }
        if (!isPlayersTurn(playerId)) {
            throw new IllegalStateException("Not your turn");
        }

        consumeTimeForSide(getSideToMove());

        if (whiteTimeRemaining <= 0) {
            timeoutWhite();
            return;
        }
        if (blackTimeRemaining <= 0) {
            timeoutBlack();
            return;
        }

        engine.applyMove(from, to);

        if (engine.isGameOver() != GameResult.ONGOING) {
            resolveResult();
            status = GameStatus.FINISHED;
        }
    }

    /**
     * Method that forcefully ends match by forfeit.
     * @param playerId {@code long} ID of a player that forfeits.
     * @throws IllegalStateException if match doesnt exist.
     * @throws IllegalArgumentException if player ID isn't in the match.
     */
    public synchronized void forfeit(long playerId) {
        if (status != GameStatus.ACTIVE) {
            throw new IllegalStateException("Game already finished");
        }

        status = GameStatus.FORFEIT;
        if (white.getId() == playerId) {
            gameResult = GameResult.BLACK_WON;
        } else if (black.getId() == playerId) {
            gameResult = GameResult.WHITE_WON;
        } else {
            throw new IllegalArgumentException("Player not in match");
        }
    }

    /**
     * Method that checks if it's player's turn.
     * @param playerId {@code long} ID of a player to check.
     * @return a {@code boolean} answering the question.
     */
    private boolean isPlayersTurn(long playerId) {
        Side side = engine.getSideToMove();
        return  (side ==Side.WHITE &&white.getId()==playerId)
              ||(side ==Side.BLACK &&black.getId()==playerId);
    }

    /**
     * Method that checks if the match concluded.
     * @return a {@code boolean} answering the question.
     */
    public boolean isOver() {
        return engine.isGameOver() != GameResult.ONGOING;
    }

    /**
     * Method that returns ID of the match.
     * @return {@code String} with a UUID of this match.
     */
    public String getMatchUUID() {
        return matchUUID;
    }

    /**
     * Method that returns FEN representation of the board.
     * @return {@code String} with a FEN representing current state of the board.
     */
    public String getFen() {
        return engine.getFen();
    }

    /**
     * Method that returns status of the match.
     * @return {@link GameStatus} enumeration representing current status of the board.
     */
    public GameStatus getStatus() {
        return status;
    }

    /**
     * Method that returns White Player.
     * @return either {@link lol.szachuz.chess.player.HumanPlayer} or {@link lol.szachuz.chess.player.ai.AiPlayer}
     *         that's controlling white pieces.
     */
    public Player getWhite() {
        return white;
    }

    /**
     * Method that returns Black Player.
     * @return either {@link lol.szachuz.chess.player.HumanPlayer} or {@link lol.szachuz.chess.player.ai.AiPlayer}
     *         that's controlling black pieces.
     */
    public Player getBlack() {
        return black;
    }

    public boolean hasAiPlayer() {
        return (white instanceof AiPlayer) || (black instanceof AiPlayer);
    }
    /**
     * Method that returns result of the match.
     * @return {@link GameResult} enumeration representing result of the match.
     */
    public GameResult getResult() {
        return gameResult;
    }

    /**
     * Method that returns which color should move next.
     * @return {@link Side} enumeration representing color of player that should move next.
     */
    public Side getSideToMove() {
        return engine.getSideToMove();
    }

    /**
     * Checks if a player is part of this match.
     * @param playerId {@code long} ID of a player to check.
     * @return an answer to the question.
     * @throws IllegalStateException if either of the Player objects in the game is null, because this shouldn't happen.
     */
    public boolean hasPlayer(long playerId) {
        if (white == null || black == null) {
            throw new IllegalStateException("One of players is null!");
        }
        return white.getId() == playerId || black.getId() == playerId;
    }

    /**
     * Retrieves {@link GameResult} of a match from the chess engine and assigns it to {@code gameResult} field.
     */
    private void resolveResult() {
        gameResult = engine.isGameOver();
    }

    /**
     * Method that removes used time for one of the sides.
     * @param side {@link Side} to subtract time for.
     */
    void consumeTimeForSide(Side side) {
        long now = System.currentTimeMillis();
        long elapsed = now - lastMoveTimestamp;

        if (side == Side.WHITE) {
            whiteTimeRemaining -= elapsed;
        } else {
            blackTimeRemaining -= elapsed;
        }

        lastMoveTimestamp = now;
    }

    /**
     * Method that handles finished timer for white player.
     */
    public void timeoutWhite() {
        status = GameStatus.FINISHED;
        gameResult = GameResult.BLACK_WON;
    }

    /**
     * Method that handles finished timer for black player.
     */
    public void timeoutBlack() {
        status = GameStatus.FINISHED;
        gameResult = GameResult.WHITE_WON;
    }

    /**
     * Method that returns time remaining for black player.
     * @return remaining time in milliseconds.
     */
    public long getBlackTimeRemaining() {
        return blackTimeRemaining;
    }

    /**
     * Method that returns time remaining for white player.
     * @return remaining time in milliseconds.
     */
    public long getWhiteTimeRemaining() {
        return whiteTimeRemaining;
    }

}
