package lol.szachuz.chess;

import jakarta.websocket.Session;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ChessSocketRegistry {

    private static final Map<String, Set<Session>> gameSessions =
            new ConcurrentHashMap<>();

    private ChessSocketRegistry() {}

    public static void register(String gameUUID, Session session) {
        gameSessions
                .computeIfAbsent(gameUUID, k -> ConcurrentHashMap.newKeySet())
                .add(session);
    }

    public static void unregister(String gameUUID, Session session) {
        Set<Session> sessions = gameSessions.get(gameUUID);
        if (sessions == null) return;

        sessions.remove(session);
        if (sessions.isEmpty()) {
            gameSessions.remove(gameUUID);
        }
    }

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
