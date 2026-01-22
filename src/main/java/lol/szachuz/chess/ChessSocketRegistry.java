package lol.szachuz.chess;

import jakarta.websocket.Session;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry of active sessions.
 * @author Rafał Kubacki
 */
public final class ChessSocketRegistry {

    private static final Map<String, Set<Session>> gameSessions =
            new ConcurrentHashMap<>();

    /**
     * Add a new session.
     * @param gameUUID {@code String} UUID of a match related to the session.
     * @param session the {@link Session} to add.
     */
    public static void register(String gameUUID, Session session) {
        gameSessions
                .computeIfAbsent(gameUUID, _ -> ConcurrentHashMap.newKeySet())
                .add(session);
    }

    /**
     * Remove session from the registry.
     * @param gameUUID {@code String} UUID of a match related to the session.
     * @param session the {@link Session} to remove.
     */
    public static void unregister(String gameUUID, Session session) {
        Set<Session> sessions = gameSessions.get(gameUUID);
        if (sessions == null) return;

        sessions.remove(session);
        if (sessions.isEmpty()) {
            gameSessions.remove(gameUUID);
        }
    }

    /**
     * Send a message JSON object to be sent to the session.
     * @param gameUUID a {@code String} UUID of a game that has message to bradcast.
     * @param message a JSON {@code String} with a message we want to send.
     */
    public static void broadcast(String gameUUID, String message) {
        Set<Session> sessions = gameSessions.get(gameUUID);
        if (sessions == null) return;

        for (Session s : sessions) {
            if (!s.isOpen()) continue;

            synchronized (s) {
                try {
                    s.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    // Fail silently, cleanup happens on @OnClose
                }
            }
        }
    }
}
