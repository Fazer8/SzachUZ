package lol.szachuz.api.dto; // przykładowa paczka

public class LeaderboardDTO {
    public String username;
    public int mmr;
    public int matchesWon;

    // Konstruktor jest WYMAGANY do zapytania "SELECT new"
    public LeaderboardDTO(String username, int mmr, int matchesWon) {
        this.username = username;
        this.mmr = mmr;
        this.matchesWon = matchesWon;
    }
}