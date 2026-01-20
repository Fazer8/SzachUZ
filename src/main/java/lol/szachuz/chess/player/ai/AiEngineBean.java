package lol.szachuz.chess.player.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ejb.Stateless;
import lol.szachuz.chess.MoveMessage;

@Stateless
public class AiEngineBean {

    public static MoveMessage computeMove(String fen, Difficulty difficulty) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = null;
        try {
            String url = "https://chess-api.com/v1";

            String json = String.format(
                    "{\"fen\":\"%s\", \"depth\":%d, \"maxThinkingTime\":%d}",
                    fen, difficulty.depth(), difficulty.thinkingTime()
            );
            for (int i = 0; i < 3; i++) {
                Thread.sleep(1000);
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

                root = mapper.readTree(body);
                if (!root.has("from") || !root.has("to")) {
                    continue;
                }

                String from = root.get("from").asText();
                String to = root.get("to").asText();

                return new MoveMessage(from, to);
            }
            throw new IllegalStateException(String.valueOf(root));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}