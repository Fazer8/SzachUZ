package lol.szachuz.matchmaking;

import jakarta.websocket.Session;

public class QueuedPlayer {
    private int userId;
    private int mmr;
    private Session session; // Sesja WebSocket, żeby wysłać info "znalazłem mecz"
    private long joinTime;

    public QueuedPlayer(int userId, int mmr, Session session) {
        this.userId = userId;
        this.mmr = mmr;
        this.session = session;
        this.joinTime = System.currentTimeMillis();
    }

    // Gettery
    public int getUserId() { return userId; }
    public int getMmr() { return mmr; }
    public Session getSession() { return session; }
    public long getJoinTime() { return joinTime; }
}