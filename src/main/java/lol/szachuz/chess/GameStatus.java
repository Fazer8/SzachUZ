package lol.szachuz.chess;

/**
 * Simple enumeration representing state of the match.
 * Can be either {@code ACTIVE} or {@code FINISHED}.
 * @author Rafał Kubacki
 */
public enum GameStatus {
    /** Game is still ongoing */
    ACTIVE,
    /** Game has concluded */
    FINISHED,
    /** Game concluded by player forfeit */
    FORFEIT
}
