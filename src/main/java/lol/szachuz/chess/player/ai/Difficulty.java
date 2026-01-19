package lol.szachuz.chess.player.ai;

public enum Difficulty {
    SILLY(50,0),
    PRO(200,5);

    private final int moveTimeMs;
    private final int skillLevel;

    Difficulty(int moveTimeMs, int skillLevel) {
        this.moveTimeMs = moveTimeMs;
        this.skillLevel = skillLevel;
    }

    public int moveTimeMs() {
        return moveTimeMs;
    }

    public int skillLevel() {
        return skillLevel;
    }
}
