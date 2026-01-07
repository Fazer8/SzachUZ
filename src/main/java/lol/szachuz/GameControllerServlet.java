package lol.szachuz;

import java.io.*;

import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import lol.szachuz.chess.MatchService;
import lol.szachuz.chess.MoveResult;

/**
 * Servlet używany do obsługi stanu aktywnej gry.
 * @author Rafał Kubacki
 */
@WebServlet(name = "GameControllerServlet", value = "/chess/move")
public class GameControllerServlet extends HttpServlet {

    public void init() {
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        //long playerId = SessionUtil.getPlayerId(req);
        long playerId = 1;
        String from = req.getParameter("from");
        String to = req.getParameter("to");

        // pierwsza strałka <--(ok)-- w gameplay LooP
        MoveResult result = MatchService.getInstance()
                .processMove(playerId, from, to);

        res.setContentType("application/json");
        res.getWriter().write(result.toJson());    }

    public void destroy() {
    }
}