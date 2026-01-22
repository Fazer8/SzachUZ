package lol.szachuz.chess.player.ai;

import lol.szachuz.chess.*;

import java.util.concurrent.*;

/**
 * Class used for generating AI moves.
 * @author Rafał Kubacki
 */
public class AiMoveScheduler {

    /**
     * Static method, that checks if game has an AI player, and generates move acordingly.
     * @param match {@link Match} to check and generate for.
     */
    public static void scheduleIfNeeded(Match match) {

        if (match == null) {
            return;
        }
        if (!match.hasAiPlayer()) {
            return;
        }

        AiPlayer ai = (AiPlayer) (match.getBlack() instanceof AiPlayer ? match.getBlack() : match.getWhite());

        try {
            if (match.getStatus() == GameStatus.FINISHED) return;

            String err = "{ \"type\": \"ERROR\", \"message\": \"" +
                    match.getFen() + "\" }";
            ChessSocketRegistry.broadcast(match.getMatchUUID(), err);
            MoveMessage move = AiEngineBean.computeMove(
                    match.getFen(),
                    ai.getSkillLevel()
            );

            MoveResult result = MatchService.getInstance()
                    .processMove(ai.getId(), move);

            ChessSocketRegistry.broadcast(match.getMatchUUID(), result.toJson());

        } catch (Exception e) {
            String err = "{ \"type\": \"ERROR\", \"message\": \"" +
                    e.getMessage() + "\" }";
            ChessSocketRegistry.broadcast(match.getMatchUUID(), err);
        }
    }
}
