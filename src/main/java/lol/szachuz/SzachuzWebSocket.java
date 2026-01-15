package lol.szachuz;

import jakarta.websocket.Session;

public interface SzachuzWebSocket {
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

    default void close(Session s) {
        try { s.close(); } catch (Exception e) {e.printStackTrace();}
    }

}
