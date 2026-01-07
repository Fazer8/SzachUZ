package lol.szachuz.matchmaking;

import jakarta.websocket.Session;

public class QueuedPlayer {
    private final int userId;
    private final int mmr;
    private final String username; // <--- NOWE POLE
    private final Session session;
    private final long joinTime;

    // Zaktualizowany konstruktor
    public QueuedPlayer(int userId, int mmr, String username, Session session) {
        this.userId = userId;
        this.mmr = mmr;
        this.username = username; // <--- ZAPISUJEMY
        this.session = session;
        this.joinTime = System.currentTimeMillis();
    }

    public int getUserId() { return userId; }
    public int getMmr() { return mmr; }
    public String getUsername() { return username; } // <--- GETTER
    public Session getSession() { return session; }
    public long getJoinTime() { return joinTime; }
}