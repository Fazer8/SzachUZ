package lol.szachuz.api;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lol.szachuz.api.dto.RegisterDTO;
import lol.szachuz.db.Repository.UsersRepository;

@Path("/auth")
public class RegisterResource {

    private final UsersRepository usersRepository;

    public RegisterResource() {
        this.usersRepository = new UsersRepository();
    }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(RegisterDTO dto) {

        if (dto == null || dto.username == null || dto.email == null || dto.password == null ||
                dto.username.isBlank() || dto.email.isBlank() || dto.password.isBlank()) {

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Missing registration data\"}")
                    .build();
        }

        String result = usersRepository.register(dto.username, dto.email, dto.password);

        if ("Username already taken".equals(result)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\":\"Username already taken\"}")
                    .build();
        }

        if ("Email already taken".equals(result)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\":\"Email already taken\"}")
                    .build();
        }

        if ("Registration failed".equals(result)) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Registration failed\"}")
                    .build();
        }

        return Response.ok("{\"message\":\"" + result + "\"}").build();
    }
}
