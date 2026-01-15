package lol.szachuz.api;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*; // Importujemy Consumes, PUT/POST itp.
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lol.szachuz.api.dto.LeaderboardDTO;
import lol.szachuz.api.dto.MmrUpdateRequest; // Zaimportuj nowe DTO
import lol.szachuz.db.Entities.Leaderboard; // Potrzebne do operacji na encji
import lol.szachuz.db.Repository.LeaderboardRepository;

import java.util.List;

@Path("/")
@RequestScoped
public class ScoreResource {

    @Inject
    LeaderboardRepository leaderboardRepository;

    @GET
    @Path("/topten")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTop10() {
        List<LeaderboardDTO> result = leaderboardRepository.findTop10();
        return Response.ok(result).build();
    }

    /**
     * Aktualizuje MMR i statystyki wygranych użytkownika.
     * Używamy POST (lub PUT), aby przesłać dane zmiany.
     */
    @POST
    @Path("/mmr-update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateMmr(MmrUpdateRequest request) {
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Brak danych żądania").build();
        }

        Leaderboard player = leaderboardRepository.findByUserId(request.getUserId());

        if (player == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Użytkownik o podanym ID nie istnieje").build();
        }

        int currentMmr = player.getMmr();
        player.setMmr(currentMmr + request.getMmrChange());

        if (request.isWin()) {
            player.setMatchesWon(player.getMatchesWon() + 1);
        }

        try {
            leaderboardRepository.update(player);

            return Response.ok("Zaktualizowano MMR dla gracza: " + player.getUser().getUsername()).build();
        } catch (Exception e) {
            return Response.serverError().entity("Błąd podczas zapisu: " + e.getMessage()).build();
        }
    }


//// Przykład użycia w logice gry (GameService)
//int myMmr = 1200;
//int opponentMmr = 1400;
//boolean iWon = true;
//
//// 1. Obliczamy zmianę
//int change = EloCalculator.calculateMmrChange(
//    myMmr,
//    opponentMmr,
//    iWon ? EloCalculator.WIN : EloCalculator.LOSS
//);
//
//// 2. Tworzymy DTO do wysłania do bazy
//MmrUpdateRequest request = new MmrUpdateRequest();
//request.setUserId(myId);
//request.setMmrChange(change); // np. +24
//request.setWin(iWon);
//
//// 3. Wysyłamy do ScoreResource.updateMmr(request)...

}