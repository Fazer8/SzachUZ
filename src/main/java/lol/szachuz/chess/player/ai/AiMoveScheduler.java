package lol.szachuz.chess.player.ai;

import lol.szachuz.chess.*;
import lol.szachuz.chess.player.Player;

import java.util.concurrent.*;

public class AiMoveScheduler {

    private static final ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor();

    private AiMoveScheduler() {}

    public static void scheduleIfNeeded(Match match) {

        if (!match.hasAiPlayer()) {
            return;
        }

        AiPlayer ai = (AiPlayer) (match.getBlack() instanceof AiPlayer ? match.getBlack() : match.getWhite());

        executor.schedule(() -> {
            try {
                if (match.getStatus() == GameStatus.FINISHED) return;

                MoveMessage move = AiEngineBean.computeMove(
                        match.getFen(),
                        ai.getSkillLevel()
                );

                MoveResult result = MatchService.getInstance()
                        .processMove(ai.getId(), move);

                ChessSocketRegistry.broadcast(match.getMatchUUID(), result.toJson());

            } catch (Exception _) {}
        }, 500, TimeUnit.MILLISECONDS);
    }
}
