package lol.szachuz.chess.player.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ejb.Stateless;
import lol.szachuz.chess.MoveMessage;

/**
 * Stateless Bean responsible for creating AI moves.
 * @see jakarta.ejb.Stateless
 * @author Rafał Kubacki
 */
@Stateless
public class AiEngineBean {

    /**
     * Method that's requesting move evaluation at chess-api.com and returns move.
     * @param fen {@code String} of current board state.
     * @param difficulty {@link Difficulty} of the AI (configuration of the engine).
     * @throws IllegalStateException if something unexpected happens.
     * @return {@link MoveMessage} of the moves AI decided to make.
     */
    public static MoveMessage computeMove(String fen, Difficulty difficulty) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = null;
        try {
            String url = "https://chess-api.com/v1";
            String json;
            for (int i = 0; i < 2; i++) {
                if (i == 1) {
                    Pattern fenPatternRegret = Pattern.compile("^([a-zA-Z0-9/]+ [a-zA-Z0-9/]+ [a-zA-Z0-9/]+ )[a-zA-Z0-9/]+( [a-zA-Z0-9/]+ [a-zA-Z0-9/]+)");
                    Matcher regretMatcher = fenPatternRegret.matcher(fen);

                    String resultString = regretMatcher.replaceAll("$1-$2");

                    json = String.format(
                            "{\"fen\":\"%s\", \"depth\":%d, \"maxThinkingTime\":%d}",
                            resultString, difficulty.depth(), difficulty.thinkingTimeMs()
                    );
                } else {
                    json = String.format(
                            "{\"fen\":\"%s\", \"depth\":%d, \"maxThinkingTime\":%d}",
                            fen, difficulty.depth(), difficulty.thinkingTimeMs()
                    );
                }
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
