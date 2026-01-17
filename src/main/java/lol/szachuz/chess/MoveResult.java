package lol.szachuz.chess;

import com.github.bhlangonijr.chesslib.Side;
import java.util.List;

public record MoveResult(
        String fen,
        GameStatus status,
        GameResult result,
        Side sideToMove,
        List<String> history
) {

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
                + "\", \"history\": " + historyJson.toString() // <--- Dodajemy
                + " }";
    }

    public static MoveResult from(Match match) {
        return new MoveResult(
                match.getFen(),
                match.getStatus(),
                match.getResult(),
                match.getSideToMove(),
                match.getMoveHistorySan() // <--- Pobieramy z meczu
        );
    }
}