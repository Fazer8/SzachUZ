package lol.szachuz.chess.player.ai;

import jakarta.ejb.Stateless;
import lol.szachuz.chess.MoveMessage;

import java.io.*;

@Stateless
public class AiEngineBean {
    public static MoveMessage computeMove(String fen, Difficulty difficulty) {
        try {
            Process process = new ProcessBuilder("stockfish").start();

            BufferedWriter in = new BufferedWriter(
                    new OutputStreamWriter(process.getOutputStream()));
            BufferedReader out = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            send(in, "uci");
            send(in, "isready");

            configureDifficulty(in, difficulty);

            send(in, "position fen " + fen);
            send(in, "go movetime " + difficulty.moveTimeMs());

            String line;
            while ((line = out.readLine()) != null) {
                if (line.startsWith("bestmove")) {
                    return MoveMessage.fromUCIString(line.split(" ")[1]);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        throw new IllegalStateException("No move returned");
    }

    private static void configureDifficulty(BufferedWriter in, Difficulty difficulty) throws IOException {

        send(in, "setoption name Skill Level value " + difficulty.skillLevel());

        if (difficulty == Difficulty.SILLY) {
            send(in, "setoption name MultiPV value 4");
        }
    }

    private static void send(BufferedWriter in, String cmd) throws IOException {
        in.write(cmd);
        in.newLine();
        in.flush();
    }
}
