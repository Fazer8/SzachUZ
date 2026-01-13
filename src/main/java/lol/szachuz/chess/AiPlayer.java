package lol.szachuz.chess;

public class AiPlayer extends Player {
    // TODO: use enum
    private final long skillLevel;
    public AiPlayer(long skillLevel) {
        this.skillLevel = skillLevel;
        super(-1);
    }
}
