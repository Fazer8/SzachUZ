package lol.szachuz.matchmaking;

import java.util.HashSet;
import java.util.Set;

public class PendingMatch {
    private String matchId;
    private QueuedPlayer player1;
    private QueuedPlayer player2;
    private Set<Integer> acceptedUserIds = new HashSet<>();
    private long creationTime;

    public PendingMatch(String matchId, QueuedPlayer p1, QueuedPlayer p2) {
        this.matchId = matchId;
        this.player1 = p1;
        this.player2 = p2;
        this.creationTime = System.currentTimeMillis();
    }

    public void accept(int userId) {
        acceptedUserIds.add(userId);
    }

    public boolean isFullyAccepted() {
        return acceptedUserIds.contains(player1.getUserId()) &&
                acceptedUserIds.contains(player2.getUserId());
    }

    public QueuedPlayer getOpponent(int userId) {
        if (player1.getUserId() == userId) return player2;
        if (player2.getUserId() == userId) return player1;
        return null;
    }

    public QueuedPlayer getPlayer1() { return player1; }
    public QueuedPlayer getPlayer2() { return player2; }
    public String getMatchId() { return matchId; }
}