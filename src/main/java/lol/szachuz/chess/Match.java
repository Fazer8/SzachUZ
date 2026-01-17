package lol.szachuz.chess;

import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.move.Move;
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

    // Historia ruchów w formacie tekstowym (e4, Nf3)
    private final List<String> moveHistorySan = new ArrayList<>();

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

        // 1. Generujemy nazwę ruchu (SAN) ZANIM go wykonamy
        // Używamy własnej metody, bo biblioteka ma z tym problemy w tej wersji
        String san = generateSan(from, to);

        // 2. Wykonujemy ruch na silniku
        engine.applyMove(from, to);

        // 3. Dodajemy nazwę ruchu do historii
        moveHistorySan.add(san);

        if (engine.isGameOver() != GameResult.ONGOING) {
            resolveResult();
            status = GameStatus.FINISHED;
        }
    }

    /**
     * Ręczna metoda generująca notację szachową (SAN).
     * Obsługuje: Pionki, Figury, Bicia (x) i Roszady (O-O, O-O-O).
     * (Uproszczona: nie obsługuje skrajnych przypadków dwuznaczności jak Raxd1, ale do projektu wystarczy).
     */
    private String generateSan(String fromStr, String toStr) {
        try {
            Square from = Square.fromValue(fromStr.toUpperCase());
            Square to = Square.fromValue(toStr.toUpperCase());
            Piece piece = engine.getBoard().getPiece(from);
            Piece target = engine.getBoard().getPiece(to);

            // 1. Obsługa Roszady (Krótkiej i Długiej)
            if (piece.getPieceType() == PieceType.KING) {
                // Białe
                if (from == Square.E1 && to == Square.G1) return "O-O";
                if (from == Square.E1 && to == Square.C1) return "O-O-O";
                // Czarne
                if (from == Square.E8 && to == Square.G8) return "O-O";
                if (from == Square.E8 && to == Square.C8) return "O-O-O";
            }

            StringBuilder san = new StringBuilder();

            // 2. Litera figury (N, B, R, Q, K). Pionek nie ma litery.
            if (piece.getPieceType() != PieceType.PAWN) {
                san.append(getPieceLetter(piece.getPieceType()));
            }

            // 3. Czy to jest bicie?
            boolean isCapture = target != Piece.NONE;

            // Specjalny przypadek: bicie w przelocie (en passant) dla pionka
            // (Jeśli pionek idzie na ukos, a pole docelowe jest puste -> to en passant)
            if (piece.getPieceType() == PieceType.PAWN && from.getFile() != to.getFile() && target == Piece.NONE) {
                isCapture = true;
            }

            if (isCapture) {
                if (piece.getPieceType() == PieceType.PAWN) {
                    // Dla pionka przy biciu dodajemy literę kolumny, z której ruszył (np. "exd5")
                    san.append(from.getFile().getNotation().toLowerCase());
                }
                san.append("x");
            }

            // 4. Pole docelowe
            san.append(to.value().toLowerCase());

            // 5. Opcjonalnie: Szach (+)
            // Sprawdzenie szacha jest trudne przed wykonaniem ruchu,
            // więc w tej uproszczonej wersji to pominiemy, żeby nie komplikować kodu.

            return san.toString();

        } catch (Exception e) {
            // Fallback w razie błędu
            return fromStr + "-" + toStr;
        }
    }

    private String getPieceLetter(PieceType type) {
        return switch (type) {
            case KNIGHT -> "N";
            case BISHOP -> "B";
            case ROOK   -> "R";
            case QUEEN  -> "Q";
            case KING   -> "K";
            default     -> "";
        };
    }

    public List<String> getMoveHistorySan() {
        return Collections.unmodifiableList(moveHistorySan);
    }

    private boolean isPlayersTurn(long playerId) {
        Side side = engine.getSideToMove();
        return  (side == Side.WHITE && white.getId() == playerId)
                ||(side == Side.BLACK && black.getId() == playerId);
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

    private void resolveResult() { gameResult = engine.isGameOver(); }
}