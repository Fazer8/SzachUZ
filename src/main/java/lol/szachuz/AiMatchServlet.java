package lol.szachuz;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lol.szachuz.auth.JWTDecoder;
import lol.szachuz.chess.Match;
import lol.szachuz.chess.MatchService;
import lol.szachuz.chess.player.HumanPlayer;
import lol.szachuz.chess.player.Player;
import lol.szachuz.chess.player.ai.AiPlayer;
import lol.szachuz.chess.player.ai.Difficulty;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/sv/training")
public class AiMatchServlet extends HttpServlet {
    private static final Map<String, Difficulty> difficulties;
    static {
        difficulties = Map.of("EASY", Difficulty.SILLY, "HARD", Difficulty.PRO);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Difficulty difficulty = difficulties.get(req.getParameter("difficulty"));
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String token = authHeader.substring("Bearer ".length());
        long userId = JWTDecoder.parseUserIdFromToken(token);
        Player white;
        Player black;
        String color;
        long aiId = (userId << 32) ^ System.nanoTime();
        if (req.getParameter("side").equals("white")) {
            white = new HumanPlayer(userId);
            black = new AiPlayer(aiId, difficulty);
            color = "WHITE";
        } else if (req.getParameter("side").equals("black")) {
            white = new AiPlayer(aiId, difficulty);
            black = new HumanPlayer(userId);
            color = "BLACK";
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().write("""
                { "type": "ERROR", "msg": "Invalid request" }
            """);
            return;
        }
        Match match = MatchService.getInstance().createMatch(white, black);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        resp.getWriter().write("""
        {
            "type": "GAME_START",
            "gameId": "%s",
            "color": "%s"
        }""".formatted(match.getMatchUUID(), color));
    }
}
