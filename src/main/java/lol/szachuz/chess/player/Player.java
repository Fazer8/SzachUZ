package lol.szachuz.chess.player;

/**
 * Abstract class representing player.
 * @author Rafał Kubacki
 */
public abstract class Player {
    private final long id;

    /**
     * Player constructor.
     * @param id {@code long} player id.
     */
    public Player(long id) {
        this.id = id;
    }

    /**
     * ID getter.
     * @return {@code long} player id.
     */
    public long getId() {
        return id;
    }

}
