package lol.szachuz.db.Entities;

import jakarta.persistence.*;

@Entity
@Table(name = "leaderboard")
public class Leaderboard {
    /**
     * Represents the unique identifier for a user within the leaderboard system.
     *
     * This field is the primary key for the "leaderboard" table in the database.
     * It is automatically generated using the IDENTITY strategy, ensuring its uniqueness.
     */
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
