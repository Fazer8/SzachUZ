package lol.szachuz.chess.player.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ejb.Stateless;
import lol.szachuz.chess.ChessSocketRegistry;
import lol.szachuz.chess.MoveMessage;

@Stateless
public class AiEngineBean {

    public static MoveMessage computeMove(String fen, Difficulty difficulty) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String url = "https://chess-api.com/v1";
            int depth = difficulty.depth();

            // HTTP request body
            String json = String.format(
                    "{\"fen\":\"%s\", \"depth\":%d}",
                    fen, depth
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            String body = response.body();

            if (status != 200) {
                throw new IllegalStateException("Status " + status);
            }

            // Parse JSON safely
            JsonNode root = mapper.readTree(body);
            if (!root.has("from") || !root.has("to")) {
                throw new IllegalStateException("Invalid API response! " + root.asText());
            }

            String from = root.get("from").asText();
            String to = root.get("to").asText();

            return new MoveMessage(from, to);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}