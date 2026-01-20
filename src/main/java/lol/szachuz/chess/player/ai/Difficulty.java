package lol.szachuz.chess.player.ai;

public enum Difficulty {
    SILLY(1),
    // NOVICE(3),
    // CHALLENGER(9),
    PRO(15);

    private final int depth;

    Difficulty(int depth) {
        this.depth = depth;
    }

    public int depth() {
        return depth;
    }
}
