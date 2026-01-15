package lol.szachuz.db.Entities;

import jakarta.persistence.*;


/**
 * Represents a leaderboard entry for a user in the system.
 *
 * This entity is mapped to the "leaderboard" table in the database, storing information
 * about a user's matchmaking rating (MMR) and the number of matches they have won.
 * Each entry is associated with a specific user in the system.
 */
@Entity
@Table(name = "leaderboard")
public class Leaderboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "userid")
    private Users user;

    private int mmr;
    private int matchesWon;


    //    <<  <<  GETTERS/SETTERS  >>  >>

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public int getMmr() {
        return mmr;
    }

    public void setMmr(int mmr) {
        this.mmr = mmr;
    }

    public int getMatchesWon() {
        return matchesWon;
    }

    public void setMatchesWon(int matchesWon) {
        this.matchesWon = matchesWon;
    }
}
