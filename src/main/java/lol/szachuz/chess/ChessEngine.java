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

    public synchronized String getFen() {
        return board.getFen();
    }

    public synchronized Side getSideToMove() {
        return board.getSideToMove();
    }
    public Board getBoard() {return this.board;}
}
