package lol.szachuz;

import lol.szachuz.chess.*;

public class GameCreationSomething {

    public static String createMatch(Player player1, Player player2) {
        Match match = MatchService.getInstance().createMatch(player1, player2);
        return match.getMatchUUID();
    }

}