package lol.szachuz.chess.player.ai;

public enum Difficulty {
    SILLY(1, 20),
    // NOVICE(3, 40),
    // CHALLENGER(9, 70),
    PRO(15, 99);

    private final int depth;
    private final int thinkingTime;

    Difficulty(int depth, int thinkingTime) {
        this.depth = depth;
        this.thinkingTime = thinkingTime;
    }

    public int depth() {
        return depth;
    }
    public int thinkingTime() {
        return thinkingTime;
    }
}
