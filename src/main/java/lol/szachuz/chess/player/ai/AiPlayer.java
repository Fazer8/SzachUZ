package lol.szachuz.chess.player.ai;

import lol.szachuz.chess.player.Player;

public class AiPlayer extends Player {

    private final Difficulty skillLevel;
    public AiPlayer(long aiId, Difficulty skillLevel) {
        super(aiId);
        this.skillLevel = skillLevel;
    }

    public Difficulty getSkillLevel() {
        return skillLevel;
    }
}
