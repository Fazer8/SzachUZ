package lol.szachuz.api;


import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lol.szachuz.api.dto.LeaderboardDTO;
import lol.szachuz.db.Repository.LeaderboardRepository;

import java.util.List;

@Path("/api")
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
}
