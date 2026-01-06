package lol.szachuz;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lol.szachuz.chess.*;

import java.io.IOException;

/**
 * Servlet używany do tworzenia gry.
 * Matchmaking powinien wywołać go z id graczy, których wylosował, lub w przypadku PvE przycisk od trenowania.
 * Po stworzeniu meczu, zostaje zwrócone <b>matchUUID</b>, które  powinno być użyte do przekierowania na <i>/chess?match=<b>matchUUID</b></i>
 * @author Rafał Kubacki
 */
@WebServlet(name = "GameCreationServlet", value = "/chess/create-game")
public class GameCreationServlet extends HttpServlet {

    public void init() {
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        // TODO: Dobre miejsce na wrzorzec budowniczego/fabryki
        try {
            String matchUUID = createMatch(req);
            res.getWriter().write("{"+
                   "\"match_id\": \"" + matchUUID + "\""
            +"}");
        } catch (IllegalArgumentException | IllegalStateException e) {
            res.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private String createMatch(HttpServletRequest req) throws IllegalArgumentException {
        String matchType = req.getParameter("match_type");
        // TODO: Można by tu chyba kolory graczom przypisać
        long player1Id = Long.parseLong(req.getParameter("player_1_id"));
        Player player1 = new HumanPlayer(player1Id);
        Player player2;
        if (matchType.equals("pvp")) {
            long player2Id = Long.parseLong(req.getParameter("player_2_id"));
            player2 = new HumanPlayer(player2Id);
        } else if (matchType.equals("pve")) {
            player2 = new AiPlayer(1);
        } else {
            throw new IllegalArgumentException("Invalid creation data\"");
        }

        Match match = MatchService.getInstance().createMatch(player1, player2);
        return match.getMatchUUID();
    }
    public void destroy() {
    }
}