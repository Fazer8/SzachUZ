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
     */
    public synchronized void applyMove(String from, String to) {
        Move move;
        Square fromSq = Square.fromValue(from.toUpperCase());
        Square toSq   = Square.fromValue(to.toUpperCase());

        if (isPromotion(fromSq, toSq)) {
            move = new Move(fromSq, toSq, defaultPromotionPiece());
        } else {
            move = new Move(fromSq, toSq);
        }

        if (isLegalMove(move)) {
            board.doMove(move);
        } else {
            throw new IllegalArgumentException("Illegal move");
        }
    }

    /**
     * Detect if promotion is needed.
     * @param from {@link Square} position of chess position.
     * @param to {@link Square} where to move.
     * @return {@code boolean} anwsering the question.
     */
    private boolean isPromotion(Square from, Square to) {
        Piece piece = board.getPiece(from);

        return piece.getPieceType() == PieceType.PAWN &&
                (to.getRank() == Rank.RANK_8 || to.getRank() == Rank.RANK_1);
    }

    /**
     * Determines which piece to promote to.
     * @return {@link Piece} that got promoted.
     */
    private Piece defaultPromotionPiece() {
        return board.getSideToMove() == Side.WHITE
                ? Piece.WHITE_QUEEN
                : Piece.BLACK_QUEEN;
    }

    /**
     * Method that checks if the match concluded.
     * @return a {@code boolean} answering the question.
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
     */
    public synchronized String getFen() {
        return board.getFen();
    }

    /**
     * Method that returns which color should move next.
     * @return {@link Side} enumeration representing color of player that should move next.
     */
    public synchronized Side getSideToMove() {
        return board.getSideToMove();
    }
    public Board getBoard() {return this.board;}
}
