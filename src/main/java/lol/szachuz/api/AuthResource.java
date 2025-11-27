package lol.szachuz.api;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import lol.szachuz.api.dto.LoginDTO;
import lol.szachuz.api.dto.RegisterDTO;

import lol.szachuz.api.dto.TokenResponse;
import lol.szachuz.api.dto.MessageResponse;

import lol.szachuz.db.Repository.UsersRepository;

@Path("/auth")
@RequestScoped
public class AuthResource {

    @Inject
    private UsersRepository usersRepository;

    // <<--->> Endpoint: REJESTRACJA <<--->>
    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(RegisterDTO request) {
        String username = request.username;
        String email = request.email;
        String password = request.password;

        if (username == null || email == null || password == null || username.isBlank() || email.isBlank() || password.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MessageResponse("Username, email, and password cannot be empty."))
                    .build();
        }

        String result = usersRepository.register(username, email, password);

        if (result.startsWith("Registration successful")) {
            return Response.status(Response.Status.CREATED)
                    .entity(new MessageResponse(result))
                    .build();
        } else if (result.equals("Username already taken") || result.equals("Email already taken")) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new MessageResponse(result))
                    .build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MessageResponse(result))
                    .build();
        }
    }

    // <<--->> Endpoint: LOGOWANIE <<--->>
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(LoginDTO request) {
        String email = request.email;
        String password = request.password;

        if (email == null || password == null || email.isBlank() || password.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MessageResponse("Email and password cannot be empty."))
                    .build();
        }
        String token = usersRepository.login(email, password);

        if (token != null) {
            return Response.ok(new TokenResponse(token)).build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new MessageResponse("Invalid email or password."))
                    .build();
        }
    }
}