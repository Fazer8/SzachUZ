package lol.szachuz.api;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.inject.Inject;
import jakarta.enterprise.context.RequestScoped;

import lol.szachuz.api.dto.LoginDTO;
import lol.szachuz.db.Repository.UsersRepository;

@Path("/auth")
@RequestScoped
public class LoginResource {

    @Inject
    private UsersRepository usersRepository;

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(LoginDTO dto) {

        if (dto == null || dto.email == null || dto.password == null ||
                dto.email.isBlank() || dto.password.isBlank()) {

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Missing login data\"}")
                    .build();
        }

        // 🔥 Używamy TWOJEJ metody repozytorium
        String token = usersRepository.login(dto.email, dto.password);

        if (token == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Invalid email or password\"}")
                    .build();
        }

        // Zwracamy token (frontend go zapisze)
        return Response.ok("{\"token\":\"" + token + "\"}").build();
    }
}
