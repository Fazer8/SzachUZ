package lol.szachuz.chess;

import com.github.bhlangonijr.chesslib.Side;

/**
 * Record {@link MoveResult} representing match state after the previous move.
 * @param fen Current chessboard state in FEN notation.
 * @param status Game state {@link GameStatus} enumeration.
 * @param result Game result {@link GameResult} enumeration representing if and how the game ended.
 * @param sideToMove {@link Side} enumeration representing color of player that is next in line to move.
 * @author Rafał Kubacki
 */
public record MoveResult(
    String fen,
    GameStatus status,
    GameResult result,
    Side sideToMove
) {

    /**
     * Method used to serialize object to JSON.
     * @return {@code String}, which is an object serialized to JSON.
     * @author Rafał Kubacki
     */
    public String toJson() {
        return "{ \"fen\": \"" + fen
            + "\", \"status\": \"" + status
            + "\", \"result\": \"" + result
            + "\", \"sideToMove\": \"" + sideToMove
        + "\" }";
    }

    /**
     * Creates {@link MoveResult} object based on current match state.
     * @param match {@link Match} object based on which to create the {@code MoveResult}.
     * @return new {@link MoveResult} object representing current match state.
     * @author Rafał Kubacki
     */
    public static MoveResult from(Match match) {
        return new MoveResult(
            match.getFen(),
            match.getStatus(),
            match.getResult(),
            match.getSideToMove()
        );
    }
}