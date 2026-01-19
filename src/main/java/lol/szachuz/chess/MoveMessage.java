package lol.szachuz.chess;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bhlangonijr.chesslib.move.Move;

import java.util.regex.Pattern;

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
     * @author Rafał Kubacki
     */
    public static MoveMessage fromJson(String json) {
        try {
            return MAPPER.readValue(json, MoveMessage.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid MOVE message");
        }
    }

    /**
     * Function used to parse a move UCI @{code String}.
     * @param uci a UCI {@code String} representing this object.
     * @return an instance of {@link MoveMessage}.
     * @throws IllegalArgumentException in case of an incorrect JSON String.
     * @author Rafał Kubacki
     */
    public static MoveMessage fromUCIString(String uci) {
        if (uci.length() != 4) {
            throw new IllegalArgumentException("Invalid MOVE message - incorrect length of " + uci.length());
        }

        Pattern patternInvalidChars = Pattern.compile("[^1-8a-h]", Pattern.CASE_INSENSITIVE);
        Pattern patternCorrectFormat = Pattern.compile("^[a-h][1-8][a-h][1-8]$", Pattern.CASE_INSENSITIVE);

        if (patternInvalidChars.matcher(uci).find()) {
            throw new IllegalArgumentException("Invalid MOVE message - invalid characters: " + uci);
        }

        if (!patternCorrectFormat.matcher(uci).find()) {
            throw new IllegalArgumentException("Invalid MOVE message - invalid format: " + uci);
        }

        return new MoveMessage(uci.substring(0,1), uci.substring(2,3));
    }
}
