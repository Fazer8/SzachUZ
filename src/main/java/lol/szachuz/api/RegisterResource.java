package lol.szachuz.api;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.inject.Inject;
import jakarta.enterprise.context.RequestScoped;

import lol.szachuz.api.dto.RegisterDTO;
import lol.szachuz.db.Repository.UsersRepository;

@Path("/auth")
@RequestScoped
public class RegisterResource {

    @Inject
    private UsersRepository usersRepository;

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(RegisterDTO dto) {

        if (dto == null ||
                dto.username == null || dto.email == null || dto.password == null ||
                dto.username.isBlank() || dto.email.isBlank() || dto.password.isBlank()) {

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Missing registration data\"}")
                    .build();
        }

        // 🔥 Wywołujemy Twoją metodę z repozytorium
        String result = usersRepository.register(dto.username, dto.email, dto.password);

        // --- Obsługa komunikatów z repository ---
        if (result.equals("Username already taken")) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\":\"Username already taken\"}")
                    .build();
        }

        if (result.equals("Email already taken")) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\":\"Email already taken\"}")
                    .build();
        }

        if (result.equals("Registration failed")) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Registration failed\"}")
                    .build();
        }

        // ✔ Sukces — repository zwraca np. "Registration successful (userId = X)"
        return Response.ok("{\"message\":\"" + result + "\"}").build();
    }
}
