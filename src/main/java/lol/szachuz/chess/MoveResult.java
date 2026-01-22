package lol.szachuz.chess;

import com.github.bhlangonijr.chesslib.Side;
import java.util.List;

/**
 * Record {@link MoveResult} representing match state after the previous move.
 * @param fen Current chessboard state in FEN notation.
 * @param status Game state {@link GameStatus} enumeration.
 * @param result Game result {@link GameResult} enumeration representing if and how the game ended.
 * @param sideToMove {@link Side} enumeration representing color of player that is next in line to move.
 * @param whiteTimeRemaining {@code long} milliseconds representing time remaining for a player.
 * @param blackTimeRemaining {@code long} milliseconds representing time remaining for a player.
 * @author Rafał Kubacki
 */
public record MoveResult(
    String fen,
    GameStatus status,
    GameResult result,
    Side sideToMove,
    long whiteTimeRemaining,
    long blackTimeRemaining,
    List<String> history
) {

    /**
     * Method used to serialize object to JSON.
     * @return {@code String}, which is an object serialized to JSON.
     */
    public String toJson() {
        StringBuilder historyJson = new StringBuilder("[");
        if (history != null) {
            for (int i = 0; i < history.size(); i++) {
                historyJson.append("\"").append(history.get(i)).append("\"");
                if (i < history.size() - 1) historyJson.append(",");
            }
        }
        historyJson.append("]");

        return "{ \"fen\": \"" + fen
            + "\", \"status\": \"" + status
            + "\", \"result\": \"" + result
            + "\", \"sideToMove\": \"" + sideToMove
            + "\", \"timeRemaining\": { \"white\": \"" + whiteTimeRemaining + "\", \"black\": \"" + blackTimeRemaining + "\"}"
        + "}";
    }

    /**
     * Creates {@link MoveResult} object based on current match state.
     * @param match {@link Match} object based on which to create the {@code MoveResult}.
     * @return new {@link MoveResult} object representing current match state.
     */
    public static MoveResult from(Match match) {
        return new MoveResult(
            match.getFen(),
            match.getStatus(),
            match.getResult(),
            match.getSideToMove(),
            match.getWhiteTimeRemaining(),
            match.getBlackTimeRemaining(),
            match.getMoveHistorySan()
        );
    }
}