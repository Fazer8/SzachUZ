package lol.szachuz.chess;

import lol.szachuz.chess.player.Player;
import lol.szachuz.email.EmailService;
import lol.szachuz.db.Repository.EmailRepository;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MatchService {

    private static final MatchService INSTANCE = new MatchService();
    private final InMemoryGameRepository repository = new InMemoryGameRepository();

    public static MatchService getInstance() {
        return INSTANCE;
    }

    public Match createMatch(Player p1, Player p2) {

        if (repository.isPlayerInGame(p1.getId()) ||
                repository.isPlayerInGame(p2.getId())) {
            throw new IllegalStateException("Player already in game");
        }

        Match match = new Match(
                UUID.randomUUID().toString(),
                p1,
                p2
        );

        repository.save(match);

        // --- WYSYŁANIE MAILI W TLE (CZYSTA WERSJA) ---
        CompletableFuture.runAsync(() -> {
            try {
                EmailRepository emailRepo = new EmailRepository();
                EmailService emailService = new EmailService();

                int id1 = (int) p1.getId();
                int id2 = (int) p2.getId();

                // Pobieramy dane
                String email1 = emailRepo.getEmailByUserId(id1);
                String email2 = emailRepo.getEmailByUserId(id2);
                String name1 = emailRepo.getUsernameByUserId(id1);
                String name2 = emailRepo.getUsernameByUserId(id2);

                // Wysyłamy, jeśli e-mail istnieje
                if (email1 != null) {
                    emailService.sendGameStartEmail(email1, name2, match.getMatchUUID());
                }

                if (email2 != null) {
                    emailService.sendGameStartEmail(email2, name1, match.getMatchUUID());
                }

            } catch (Exception e) {
                // Wypisujemy błąd tylko na czerwono (err), żeby nie śmiecić, ale wiedzieć o awarii
                System.err.println("Błąd w procesie wysyłania maili: " + e.getMessage());
                e.printStackTrace();
            }
        });
        // -----------------------------------------------------------

        return match;
    }

    public MoveResult processMove(long playerId, String from, String to) {
        Match match = repository.findByPlayer(playerId);

        if (match == null) {
            throw new IllegalStateException("No active match");
        }

        match.applyMove(playerId, from, to);

        if (match.getStatus() == GameStatus.FINISHED || match.isOver()) {
            repository.archive(match);
        }

        return MoveResult.from(match);
    }

    public Match loadMatchByPlayerId(long playerId) {
        return repository.findByPlayer(playerId);
    }

    public Match loadMatchByMatchId(String gameId) {
        return repository.findById(gameId);
    }
}