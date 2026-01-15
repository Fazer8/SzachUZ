package lol.szachuz.chess.player.ai;

import lol.szachuz.chess.player.Player;

public class AiPlayer extends Player {
    // TODO: use enum
    private final long skillLevel;
    public AiPlayer(long aiId, long skillLevel) {
        super(aiId);
        this.skillLevel = skillLevel;
    }

    public long getSkillLevel() {
        return skillLevel;
    }

}
