package lol.szachuz.chess.player.ai;

import lol.szachuz.chess.player.Player;

/**
 * AI Player class, extends {@link Player}.
 * @author Rafał Kubacki
 */
public class AiPlayer extends Player {

    private final Difficulty skillLevel;
    /**
     * Constructor of Ai Player.
     * @param aiId {@code long} id gracza.
     * @param skillLevel {@link Difficulty} poziom umiejętności AI.
     */
    public AiPlayer(long aiId, Difficulty skillLevel) {
        super(aiId);
        this.skillLevel = skillLevel;
    }

    /**
     * Difficulty getter.
     * @return {@link Difficulty} enum.
     */
    public Difficulty getSkillLevel() {
        return skillLevel;
    }
}
