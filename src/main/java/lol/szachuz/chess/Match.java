package lol.szachuz.chess;

import com.github.bhlangonijr.chesslib.*;
import lol.szachuz.chess.player.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Match {
    private final ChessEngine engine;
    private final Player white, black;
    private final String matchUUID;
    private GameStatus status = GameStatus.ACTIVE;
    private GameResult gameResult;

    private final List<String> moveHistorySan = new ArrayList<>();
    private final List<MoveLog> moveHistoryDetails = new ArrayList<>();

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

        Square fromSq = Square.fromValue(from.toUpperCase());
        Square toSq = Square.fromValue(to.toUpperCase());
        Piece piece = engine.getBoard().getPiece(fromSq);
        Piece target = engine.getBoard().getPiece(toSq);

        String pieceCode = getPieceCode(piece);

        boolean isCapture = (target != Piece.NONE);
        boolean isCastling = false;
        boolean isPromotion = false;

        if (piece.getPieceType() == PieceType.KING && Math.abs(fromSq.getFile().ordinal() - toSq.getFile().ordinal()) > 1) {
            isCastling = true;
        }

        if (piece.getPieceType() == PieceType.PAWN) {
            if ((piece.getPieceSide() == Side.WHITE && toSq.getRank() == Rank.RANK_8) ||
                    (piece.getPieceSide() == Side.BLACK && toSq.getRank() == Rank.RANK_1)) {
                isPromotion = true;
            }
        }

        String san = generateSan(from, to);
        engine.applyMove(from, to);
        moveHistorySan.add(san);


        moveHistoryDetails.add(new MoveLog(pieceCode, from, to, isCapture, isCastling, isPromotion));

        if (engine.isGameOver() != GameResult.ONGOING) {
            resolveResult();
            status = GameStatus.FINISHED;
        }
    }

    private String getPieceCode(Piece piece) {
        return switch (piece.getPieceType()) {
            case KNIGHT -> "N";
            case BISHOP -> "B";
            case ROOK   -> "R";
            case QUEEN  -> "Q";
            case KING   -> "K";
            default     -> "P";
        };
    }


    private String generateSan(String fromStr, String toStr) {
        try {
            Square from = Square.fromValue(fromStr.toUpperCase());
            Square to = Square.fromValue(toStr.toUpperCase());
            Piece piece = engine.getBoard().getPiece(from);
            Piece target = engine.getBoard().getPiece(to);
            if (piece.getPieceType() == PieceType.KING && Math.abs(from.getFile().ordinal() - to.getFile().ordinal()) > 1) {

                return (to.getFile() == File.FILE_G) ? "O-O" : "O-O-O";
            }
            StringBuilder san = new StringBuilder();
            if (piece.getPieceType() != PieceType.PAWN) san.append(getPieceCode(piece));
            if (target != Piece.NONE) san.append("x");
            san.append(to.value().toLowerCase());
            return san.toString();
        } catch (Exception e) { return fromStr + "-" + toStr; }
    }

    private boolean isPlayersTurn(long playerId) {
        Side side = engine.getSideToMove();
        return (side == Side.WHITE && white.getId() == playerId) || (side == Side.BLACK && black.getId() == playerId);
    }

    public boolean isOver() { return engine.isGameOver() != GameResult.ONGOING; }
    public String getMatchUUID() { return matchUUID; }
    public String getFen() { return engine.getFen(); }
    public GameStatus getStatus() { return status; }
    public Player getWhite() { return white; }
    public Player getBlack() { return black; }
    public GameResult getResult() { return gameResult; }
    public Side getSideToMove() { return engine.getSideToMove(); }

    public boolean hasPlayer(long playerId) {
        if (white == null || black == null) return false;
        return white.getId() == playerId || black.getId() == playerId;
    }

    public List<String> getMoveHistorySan() { return Collections.unmodifiableList(moveHistorySan); }
    public List<MoveLog> getMoveHistoryDetails() { return Collections.unmodifiableList(moveHistoryDetails); }

    private void resolveResult() { gameResult = engine.isGameOver(); }

    public static class MoveLog {
        public final String pieceCode;
        public final String from;
        public final String to;
        public final boolean isCapture;
        public final boolean isCastling;
        public final boolean isPromotion;

        public MoveLog(String pieceCode, String from, String to, boolean isCapture, boolean isCastling, boolean isPromotion) {
            this.pieceCode = pieceCode;
            this.from = from;
            this.to = to;
            this.isCapture = isCapture;
            this.isCastling = isCastling;
            this.isPromotion = isPromotion;
        }
    }
}