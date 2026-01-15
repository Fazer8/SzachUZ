package lol.szachuz.chess;

import com.fasterxml.jackson.databind.ObjectMapper;

public record MoveMessage(String from, String to) {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static MoveMessage fromJson(String json) {
        try {
            return MAPPER.readValue(json, MoveMessage.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid MOVE message");
        }
    }
}
