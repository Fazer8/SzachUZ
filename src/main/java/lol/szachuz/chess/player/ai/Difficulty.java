package lol.szachuz.chess.player.ai;

/**
 * Difficulty of the AI.
 * Can be either {@code SILLY} (thinking depth of 1 and max thinging time 20ms) or {@code PRO} (thinking depth of 15 and max thinging time 99ms).
 * @author Rafał Kubacki
 */
public enum Difficulty {
    /** Very easy */
    SILLY(1, 20),
    // NOVICE(3, 40),
    // CHALLENGER(9, 70),
    /** Chess Master level */
    PRO(15, 99);

    private final int depth;
    private final int thinkingTimeMs;

    /**
     * Constructor of the enum.
     * @param depth {@code int} representing how deep can it plan moves, max 18.
     * @param thinkingTimeMs {@code int} how long can it look for moves, max 100ms.
     */
    Difficulty(int depth, int thinkingTimeMs) {
        this.depth = Math.min(depth, 18);
        this.thinkingTimeMs = Math.min(thinkingTimeMs, 100);
    }

    /**
     * Depth getter.
     * @return {@code int} depth.
     */
    public int depth() {
        return depth;
    }

    /**
     * Thinking time getter.
     * @return {@code int} thinking time.
     */
    public int thinkingTimeMs() {
        return thinkingTimeMs;
    }
}
