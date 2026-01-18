package lol.szachuz.chess;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Record {@link MoveMessage} representing a move player wants to make.
 * @param from a {@code String} representing starting position of a piece that player wants to move, in FEN notation.
 * @param to a {@code String} representing where to move that piece, in FEN notation.
 * @author Rafał Kubacki
 */
public record MoveMessage(String from, String to) {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Function used to parse a JSON @{code String} recieved from frontend.
     * @param json a JSON {@code String} representing this object.
     * @return an instance of {@link MoveMessage}.
     * @throws IllegalArgumentException in case of an incorrect JSON String.
     */
    public static MoveMessage fromJson(String json) {
        try {
            return MAPPER.readValue(json, MoveMessage.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid MOVE message");
        }
    }
}
