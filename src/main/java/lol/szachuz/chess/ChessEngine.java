package lol.szachuz.chess;
import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.move.*;

public class ChessEngine {

    private final Board board;

    public ChessEngine() {
        this.board = new Board();
    }

    public synchronized boolean isLegalMove(Move move) {
        return board.isMoveLegal(move, true);
    }

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

    public synchronized boolean isGameOver() {
        return board.isMated() || board.isStaleMate();
    }

    public synchronized String getFen() {
        return board.getFen();
    }

    public synchronized Side getSideToMove() {
        return board.getSideToMove();
    }
}
