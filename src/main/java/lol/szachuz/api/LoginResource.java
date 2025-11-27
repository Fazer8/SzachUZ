package lol.szachuz.api;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lol.szachuz.api.dto.LoginDTO;
import lol.szachuz.db.Repository.UsersRepository;

@Path("/auth")
public class LoginResource {

    private final UsersRepository usersRepository;

    public LoginResource() {
        this.usersRepository = new UsersRepository(); // ręczne tworzenie repo
    }

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

        String token = usersRepository.login(dto.email, dto.password);

        if (token == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Invalid email or password\"}")
                    .build();
        }

        return Response.ok("{\"token\":\"" + token + "\"}").build();
    }
}
