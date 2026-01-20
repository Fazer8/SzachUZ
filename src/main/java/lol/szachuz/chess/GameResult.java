package lol.szachuz.chess;

/**
 * Enumeration representing possible game results.
 * Can be either {@code ONGOING}, {@code WHITE_WON}, {@code BLACK_WON} or {@code DRAW}.
 * @author Rafał Kubacki
 */
public enum GameResult {
    /** Player with white pieces won */
    WHITE_WON,
    /** Player with black pieces won */
    BLACK_WON,
    /** Game concluded with a draw or a stalemate */
    DRAW,
    /** Game is still ongoing */
    ONGOING
}