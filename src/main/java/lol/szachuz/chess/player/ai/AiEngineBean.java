package lol.szachuz.chess.player.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import jakarta.ejb.Stateless;
import lol.szachuz.chess.ChessSocketRegistry;
import lol.szachuz.chess.MoveMessage;

@Stateless
public class AiEngineBean {

    public static MoveMessage computeMove(String fen, String matchUUID, Difficulty difficulty) {
        String err = "{ \"type\": \"ERROR\", \"message\": \"" +
                "please compute me some move" + "\" }";
        ChessSocketRegistry.broadcast(matchUUID, err);
        try {
            int depth = difficulty.depth();

            String encodedFen = URLEncoder.encode(fen, StandardCharsets.UTF_8);

            String url = "https://lichess.org/api/cloud-eval?fen=" + encodedFen + "&multiPv=1&depth=" + depth;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            err = "{ \"type\": \"ERROR\", \"message\": \"" +
                    "pre-request" + "\" }"; // WORKS UP TO THERE!

            ChessSocketRegistry.broadcast(matchUUID, err);
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();

            err = "{ \"type\": \"ERROR\", \"message\": \"" +
                    body + "\" }";
            ChessSocketRegistry.broadcast(matchUUID, err);

            int start = body.indexOf("\"bestMove\":\"") + 12;
            int end = body.indexOf("\"", start);
            if (start < 12 || end < 0) {
                throw new IllegalStateException("Failed to parse bestMove from Lichess response: " + body);
            }

            String moveUCI = body.substring(start, end);

            return MoveMessage.fromUCIString(moveUCI);

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}