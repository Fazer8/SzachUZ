package lol.szachuz.chess;
import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.move.*;

/**
 * A wrapper class for {@link com.github.bhlangonijr.chesslib}.
 * @author Rafał Kubacki
 */
public final class ChessEngine {

    private final Board board;

    public ChessEngine() {
        this.board = new Board();
    }

    /**
     * Validates if the move is allowed.
     * @param move a {@link Move} to be checked.
     * @return a {@code boolean} anwsering that question.
     */
    public synchronized boolean isLegalMove(Move move) {
        return board.isMoveLegal(move, true);
    }

    /**
     * Method that tries to apply move request sent by player
     * @param from {@code String} which piece to move, in FEN notation.
     * @param to {@code String} where to move that piece, in FEN notation.
     * @throws IllegalArgumentException if player tried to make an illegal move.
     * @author Rafał Kubacki
     */
    public synchronized void applyMove(String from, String to) {
        Move move = new Move(
                Square.fromValue(from.toUpperCase()),
                Square.fromValue(to.toUpperCase())
        );
        if (isLegalMove(move)) {
            board.doMove(move);
        } else {
            throw new IllegalArgumentException("Illegal move");
        }
    }

    /**
     * Method that checks if the match concluded.
     * @return a {@code boolean} answering the question.
     * @author Rafał Kubacki
     */
    public synchronized GameResult isGameOver() {
        if (board.isMated()) {
            return switch (getSideToMove().flip()) {
                case Side.WHITE -> GameResult.WHITE_WON;
                case Side.BLACK -> GameResult.BLACK_WON;
            };
        } else if (board.isStaleMate() || board.isDraw()) {
            return GameResult.DRAW;
        }
        return GameResult.ONGOING;
    }

    /**
     * Method that returns FEN representation of the board.
     * @return {@code String} with a FEN representing current state of the board.
     * @author Rafał Kubacki
     */
    public synchronized String getFen() {
        return board.getFen();
    }

    /**
     * Method that returns which color should move next.
     * @return {@link Side} enumeration representing color of player that should move next.
     * @author Rafał Kubacki
     */
    public synchronized Side getSideToMove() {
        return board.getSideToMove();
    }
}
