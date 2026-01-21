package lol.szachuz.chess.player;

/**
 * Abstract class representing player.
 * @author Rafał Kubacki
 */
public abstract class Player {
    private final long id;

    /**
     * Player constructor.
     * @param {@code long} player id.
     * @author Rafał Kubacki
     */
    public Player(long id) {
        this.id = id;
    }

    /**
     * ID getter.
     * @return {@code long} player id.
     * @author Rafał Kubacki
     */
    public long getId() {
        return id;
    }

}
