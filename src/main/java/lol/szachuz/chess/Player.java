package lol.szachuz.chess;

public abstract class Player {
    private long id;
    public long getId() {
        return id;
    }
    public Player(long id) {
        this.id = id;
    }
}
