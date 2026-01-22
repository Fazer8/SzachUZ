package lol.szachuz;

import jakarta.websocket.Session;

/**
 * Interface with some methods used in all {@code WebSocket} classes in this project.
 * @author Rafał Kubacki
 */
public interface SzachuzWebSocket {
    /**
     * Tries to send a JSON with an Error Message to a WebSocket Session.
     * @param message a {@code String} which is the message we want to send.
     * @param session the open Session object we want to send the message to.
     */
    default void sendError(String message, Session session) {
        try {
            if (session != null && session.isOpen()) {
                String json = "{ \"type\": \"ERROR\", \"message\": \"" +
                        message.replace("\"", "'") + "\" }";
                synchronized (session) {
                    session.getBasicRemote().sendText(json);
                }
            }
        } catch (Exception ignored) {}
    }

    /**
     * Closes a WebSocket Session.
     * @param s the Session we want to close.
     */
    default void close(Session s) {
        try { s.close(); } catch (Exception e) {e.printStackTrace();}
    }

}
