package lol.szachuz.chess.player.ai;

import lol.szachuz.chess.*;
import lol.szachuz.chess.player.Player;

import java.util.concurrent.*;

public class AiMoveScheduler {

    private static final ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor();

    private AiMoveScheduler() {}

    public static void scheduleIfNeeded(Match match) {

        //Player black = match.getBlack();
        Player black = null;
        if (!(black instanceof AiPlayer ai)) return;

        executor.schedule(() -> {
            try {
                if (match.getStatus() == GameStatus.FINISHED) return;

                // Very naive placeholder
                AiMove move = AiEngine.computeMove(
                        match.getFen(),
                        ai.getSkillLevel()
                );

                MoveResult result = MatchService.getInstance()
                        .processMove(ai.getId(), move.from(), move.to());

                ChessSocketRegistry.broadcast(
                        match.getMatchUUID(),
                        result.toJson()
                );

            } catch (Exception ignored) {}
        }, 500, TimeUnit.MILLISECONDS);
    }
}
