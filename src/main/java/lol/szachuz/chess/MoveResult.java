package lol.szachuz.chess;

public class MoveResult {

    private final String fen;
    private final GameStatus status;

    public MoveResult(String fen, GameStatus status) {
        this.fen = fen;
        this.status = status;
    }

    public String toJson() {
        return "{ \"fen\": \"" + fen + "\", \"status\": \"" + status + "\" }";
    }

    public static MoveResult from(Match match) {
        return new MoveResult(match.getFen(), match.getStatus());
    }
}