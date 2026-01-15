package lol.szachuz.chess;

import com.github.bhlangonijr.chesslib.Side;
import lol.szachuz.chess.player.Player;

/**
 * Klasa reprezentująca Sesję Gry (GameSession)
 *
 * @author Rafał Kubacki
 */
public class Match {
    private final ChessEngine engine;
    private final Player white, black;
    private final String matchUUID;
    private GameStatus status = GameStatus.ACTIVE;

    private GameResult gameResult;

    public Match(String matchUUID, Player white, Player black) {
        engine = new ChessEngine();
        this.black = black;
        this.white = white;
        this.matchUUID = matchUUID;
        this.gameResult = GameResult.ONGOING;
    }

    public synchronized void applyMove(long playerId, String from, String to) {
        if (status == GameStatus.FINISHED) {
            throw new IllegalStateException("Game already finished");
        }
        if (!isPlayersTurn(playerId)) {
            throw new IllegalStateException("Not your turn");
        }

        engine.applyMove(from, to);

        if (engine.isGameOver() != GameResult.ONGOING) {
            resolveResult();
            status = GameStatus.FINISHED;
        }
    }

    private boolean isPlayersTurn(long playerId) {
        Side side = engine.getSideToMove();
        return  (side ==Side.WHITE &&white.getId()==playerId)
              ||(side ==Side.BLACK &&black.getId()==playerId);
    }

    public boolean isOver() {
        return engine.isGameOver() != GameResult.ONGOING;
    }

    public String getMatchUUID() {
        return matchUUID;
    }

    public String getFen() {
        return engine.getFen();
    }

    public GameStatus getStatus() {
        return status;
    }

    public Player getWhite() {
        return white;
    }

    public Player getBlack() {
        return black;
    }

    public GameResult getResult() {
        return gameResult;
    }

    public Side getSideToMove() {
        return engine.getSideToMove();
    }

    public boolean hasPlayer(long playerId) {
        if (white == null || black == null) {
            throw new IllegalStateException("One of players is null!");
        }
        return white.getId() == playerId || black.getId() == playerId;
    }

    private void resolveResult() {
        gameResult = engine.isGameOver();
    }
}
