package lol.szachuz.api;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import lol.szachuz.api.dto.LoginDTO;
import lol.szachuz.api.dto.RegisterDTO;

import lol.szachuz.api.dto.TokenResponse;
import lol.szachuz.api.dto.MessageResponse;

import lol.szachuz.db.Repository.UsersRepository;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;

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


        if (!verifyCaptcha(request.captcha)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MessageResponse("Captcha verification failed."))
                    .build();
        }

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


        if (!verifyCaptcha(request.captcha)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MessageResponse("Captcha verification failed."))
                    .build();
        }
        if (email == null || password == null || email.isBlank() || password.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MessageResponse("Email and password cannot be empty."))
                    .build();
        }
        String token = usersRepository.login(email, password);

        if (token != null) {
            NewCookie cookie = new NewCookie.Builder("authToken")
                    .value(token)
                    .path("/")
                    .maxAge(3600 * 24)
                    .secure(false)    // lub true jeśli HTTPS
                    .httpOnly(false)
                    .sameSite(NewCookie.SameSite.LAX)
                    .build();

            return Response.ok(new TokenResponse(token))
                    .cookie(cookie)
                    .build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new MessageResponse("Invalid email or password."))
                    .build();
        }


    }

    private boolean verifyCaptcha(String token) {
        try {
            // ZAMIAST TEGO:
            // String secretKey = "6Lc8GigsAAAAAMAKR2ZeX3CZXfwv1f1AZDNBF4FF";

            // ZRÓB TAK:
            String secretKey = System.getenv("RECAPTCHA_SECRET_KEY");

            if (secretKey == null || secretKey.isEmpty()) {
                System.out.println("BŁĄD: Brak zmiennej RECAPTCHA_SECRET_KEY!");
                return false;
            }
            String url = "https://www.google.com/recaptcha/api/siteverify";

            String params = "secret=" + secretKey + "&response=" + token;

            var conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.getOutputStream().write(params.getBytes());

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String input;
            StringBuilder response = new StringBuilder();
            while ((input = in.readLine()) != null) {
                response.append(input);
            }

            return response.toString().contains("\"success\": true");

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @POST
    @Path("/check-username")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkUsername(RegisterDTO request) {
        if (request.username == null || request.username.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MessageResponse("Username cannot be empty"))
                    .build();
        }

        boolean exists = usersRepository.existsByUsername(request.username);

        if (exists) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new MessageResponse("Username already taken"))
                    .build();
        }

        return Response.ok(new MessageResponse("Username available")).build();
    }

    @POST
    @Path("/check-email")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkEmail(RegisterDTO request) {
        if (request.email == null || request.email.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MessageResponse("Email cannot be empty"))
                    .build();
        }

        boolean exists = usersRepository.existsByEmail(request.email);

        if (exists) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new MessageResponse("Email already taken"))
                    .build();
        }

        return Response.ok(new MessageResponse("Email available")).build();
    }

}