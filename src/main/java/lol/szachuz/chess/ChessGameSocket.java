package lol.szachuz.chess;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lol.szachuz.SzachuzWebSocket;
import lol.szachuz.auth.JWTDecoder;
import lol.szachuz.chess.player.ai.AiMoveScheduler;

import java.io.EOFException;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@ServerEndpoint("/ws/chess")
public class ChessGameSocket implements SzachuzWebSocket {

    private Session session;
    private long playerId;
    private String gameUUID;

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;

        try {
            Map<String, List<String>> params = session.getRequestParameterMap();
            this.gameUUID = params.get("gameId").getFirst();
            this.playerId = JWTDecoder.parseUserIdFromToken(params.get("token").getFirst());

            // For the love of God, why does this one return null, while the other one works and match id is the same???
            Match match = MatchService.getInstance().loadMatchByMatchId(gameUUID);

            if (match == null) {
                sendError("Invalid session" + this.gameUUID + ". Match is null.", session);
                session.close();
                return;
            } else {
                sendError("Debug message for:" + this.playerId + "match is ok."
                        , session);
            }

            if(!match.hasPlayer(playerId)) {
                sendError("match.hasPlayer(" + this.playerId + "): " + match.hasPlayer(playerId), session);
                session.close();
                return;
            }

            ChessSocketRegistry.register(gameUUID, session);

            // Send initial state
            ChessSocketRegistry.broadcast(
                    gameUUID,
                    MoveResult.from(match).toJson()
            );

        } catch (Exception e) {
            sendError(e.getMessage(), session);
            close(session);
        }
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            MoveMessage move = MoveMessage.fromJson(message);

            MoveResult result = MatchService.getInstance().processMove(
                    playerId,
                    move.from(),
                    move.to()
            );

            ChessSocketRegistry.broadcast(gameUUID, result.toJson());

            //AiMoveScheduler.scheduleIfNeeded(
            //        MatchService.getInstance().loadMatchByMatchId(gameUUID) // basing on player's color, ai scheduler should receive oposite color
            //);
        } catch (Exception e) {
            sendError(e.getMessage(), session);
        }
    }

    @OnClose
    public void onClose() {
        // No immediate game removal
        // AFK logic will handle this later
        // Maybe send code for PDF generation, I don't know
        sendError("closed my testicles lolz", session);
    }

    @OnError
    public void onError(Session session, Throwable t) {
        if (! (t instanceof EOFException)) {
            sendError(t.getMessage(), session);
        }
    }
}
