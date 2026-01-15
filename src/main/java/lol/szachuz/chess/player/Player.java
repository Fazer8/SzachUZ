package lol.szachuz.chess.player;

public abstract class Player {
    private final long id;
    //private final Side side;

    public Player(long id/*, Side side*/) {
        this.id = id;
        //this.side = side;
    }

    public long getId() {
        return id;
    }

//    public Side getSide() {
//        return side;
//    }
}
