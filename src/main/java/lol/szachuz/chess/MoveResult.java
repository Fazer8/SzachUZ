package lol.szachuz.chess;

import com.github.bhlangonijr.chesslib.Side;

/**
 * Rekord {@link MoveResult} reprezentujący stan meczu
 *         po ostatnim (poprzednim) ruchu.
 * @param fen Stan szachownicy w notacji FEN powstały w wyniku ruchu gracza
 * @param status Enumeracja będąca stanem gry.
 * @param result Enumeracja opisująca wynik meczu.
 * @param sideToMove Enumeracja reprezentująca sronę gry.
 * @author Rafał Kubacki
 */
public record MoveResult(
    String fen,
    GameStatus status,
    GameResult result,
    Side sideToMove
) {

    /**
     * Metoda serializująca, zwraca JSON string reprezentujący ten obiekt.
     * @return {@code String}, będący serializacją obiektu do formatu JSON.
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
     * Tworzy obiekt {@link MoveResult} reprezentujący rezultat
     * ostatniego wykonanego ruchu w podanym meczu.
     * @param match obiekt meczu, na podstawie którego wyznaczany jest wynik.
     * @return nowy obiekt {@link MoveResult} odzwierciedlający aktualny stan meczu
     *         po ostatnim ruchu
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