package lol.szachuz.api.dto;


public class LeaderboardDTO {
    public String username;
    public int mmr;
    public int matchesWon;

    public LeaderboardDTO(String username, int mmr, int matchesWon) {
        this.username = username;
        this.mmr = mmr;
        this.matchesWon = matchesWon;
    }
}