package lol.szachuz.api;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.MediaType;
import lol.szachuz.db.Entities.Users;
import lol.szachuz.api.dto.ProfileDTO;
import jakarta.enterprise.context.RequestScoped;
// import org.eclipse.microprofile.jwt.JsonWebToken; // <-- USUWAMY TO, BO TOMCAT TEGO NIE MA
import jakarta.json.bind.annotation.JsonbProperty;
import lol.szachuz.api.dto.ChangePasswordDTO;
import lol.szachuz.db.Entities.UserPreferences;
import lol.szachuz.db.Repository.UsersRepository;
import lol.szachuz.db.Repository.UserPreferencesRepository;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Path("/profile")
@RequestScoped
// @RolesAllowed({"USER"}) // <-- ODKOMENTUJESZ TO, JAK JUŻ ZADZIAŁA POBIERANIE ID
public class ProfileResource {

    @Inject
    private UsersRepository usersRepository;

    @Inject
    private UserPreferencesRepository userPreferencesRepository;

    // USUNIĘTE: @Inject private JsonWebToken jwt;
    // ZAMIAST TEGO WSTRZYKUJEMY NAGŁÓWKI HTTP:
    @Context
    private HttpHeaders headers;

    private int getAuthenticatedUserId() {
        // 1. Pobierz nagłówek
        String authHeader = headers.getHeaderString("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new WebApplicationException("Missing or invalid Authorization header", Response.Status.UNAUTHORIZED);
        }

        try {
            // 2. Wyciągnij token (usuń "Bearer ")
            String token = authHeader.substring(7);

            // 3. Rozbij token na 3 części (Header.Payload.Signature)
            String[] chunks = token.split("\\.");
            if (chunks.length < 2) {
                throw new WebApplicationException("Invalid JWT format", Response.Status.UNAUTHORIZED);
            }

            // 4. Zdekoduj Payload (środek) z Base64
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payloadJson = new String(decoder.decode(chunks[1]), StandardCharsets.UTF_8);

            // System.out.println("DEBUG Payload: " + payloadJson); // Możesz odkomentować do debugowania

            // 5. Znajdź "sub" (subject) w JSONie "na piechotę" (bez biblioteki JSON dla uproszczenia)
            // Szukamy fragmentu "sub":"123"
            String searchKey = "\"sub\"";
            int subIndex = payloadJson.indexOf(searchKey);

            if (subIndex != -1) {
                // Znajdź początek wartości (za dwukropkiem i ewentualnymi spacjami)
                int startQuote = payloadJson.indexOf("\"", subIndex + searchKey.length());
                while (payloadJson.charAt(startQuote) != '"' && startQuote < payloadJson.length()) {
                    startQuote++;
                }
                // Znajdź koniec wartości
                int endQuote = payloadJson.indexOf("\"", startQuote + 1);

                if (startQuote != -1 && endQuote != -1) {
                    String subValue = payloadJson.substring(startQuote + 1, endQuote);
                    return Integer.parseInt(subValue);
                }
            }
            throw new WebApplicationException("Token missing 'sub' claim", Response.Status.UNAUTHORIZED);

        } catch (Exception e) {
            System.err.println("Token parsing error: " + e.getMessage());
            throw new WebApplicationException("Invalid token", Response.Status.UNAUTHORIZED);
        }
    }

    @GET
    @Path("/me")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMyProfile() {
        int userId = getAuthenticatedUserId();

        Users user = usersRepository.findById(userId);
        UserPreferences prefs = userPreferencesRepository.findByUserId(userId);

        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        ProfileDTO profileDTO = new ProfileDTO(user, prefs);
        return Response.ok(profileDTO).build();
    }

    // --- PONIŻEJ BEZ ZMIAN (tylko upewnij się, że używasz getAuthenticatedUserId w każdej metodzie) ---

    public static class UpdateFieldDTO {
        @JsonbProperty("value")
        public String value;

        @JsonbProperty("darkMode")
        public Boolean darkMode;
    }

    @PUT
    @Path("/me/username")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUsername(UpdateFieldDTO dto) {
        if (dto == null || dto.value == null || dto.value.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        int userId = getAuthenticatedUserId();
        Users user = usersRepository.findById(userId);
        if (user == null) return Response.status(Response.Status.NOT_FOUND).build();

        user.setUsername(dto.value);
        usersRepository.update(user);
        return Response.ok().build();
    }

    @PUT
    @Path("/me/password")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePassword(ChangePasswordDTO dto) {
        if (dto == null || dto.oldPassword == null || dto.newPassword == null ||
                dto.oldPassword.isEmpty() || dto.newPassword.length() < 8) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid password data").build();
        }
        int userId = getAuthenticatedUserId();
        Users user = usersRepository.findById(userId);
        if (user == null) return Response.status(Response.Status.NOT_FOUND).build();

        if (!usersRepository.checkPassword(user, dto.oldPassword)) {
            return Response.status(Response.Status.FORBIDDEN).entity("Incorrect old password.").build();
        }
        usersRepository.updatePassword(user, dto.newPassword);
        return Response.ok().entity("Password changed successfully.").build();
    }

    @PUT
    @Path("/me/darkMode")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateDarkMode(UpdateFieldDTO dto) {
        if (dto == null || dto.darkMode == null) return Response.status(Response.Status.BAD_REQUEST).build();
        int userId = getAuthenticatedUserId();
        UserPreferences prefs = userPreferencesRepository.findByUserId(userId);
        if (prefs == null) return Response.status(Response.Status.NOT_FOUND).build();

        prefs.setDarkMode(dto.darkMode);
        userPreferencesRepository.update(prefs);
        return Response.ok().build();
    }

    @PUT
    @Path("/me/language")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateLanguage(UpdateFieldDTO dto) {
        if (dto == null || dto.value == null) return Response.status(Response.Status.BAD_REQUEST).build();
        int userId = getAuthenticatedUserId();
        UserPreferences prefs = userPreferencesRepository.findByUserId(userId);
        if (prefs == null) return Response.status(Response.Status.NOT_FOUND).build();

        try {
            prefs.setLanguage(UserPreferences.Lang.valueOf(dto.value.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        userPreferencesRepository.update(prefs);
        return Response.ok().build();
    }

    @PUT
    @Path("/me/avatar")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateAvatar(@FormParam("file") InputStream uploadedInputStream,
                                 @FormParam("filename") String originalFilename) {
        if (uploadedInputStream == null || originalFilename == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        int userId = getAuthenticatedUserId();
        UserPreferences prefs = userPreferencesRepository.findByUserId(userId);
        if (prefs == null) return Response.status(Response.Status.NOT_FOUND).build();

        String ext = "png";
        if(originalFilename.toLowerCase().endsWith(".jpg") || originalFilename.toLowerCase().endsWith(".jpeg")) ext = "jpg";

        String avatarsDir = "/opt/szachuz/avatars/";
        new File(avatarsDir).mkdirs();
        String filename = "avatar_" + userId + "." + ext;

        try (OutputStream out = new FileOutputStream(new File(avatarsDir, filename))) {
            uploadedInputStream.transferTo(out);
        } catch (Exception e) {
            return Response.serverError().build();
        }
        prefs.setUserAvatar(filename);
        userPreferencesRepository.update(prefs);
        return Response.ok("{\"message\":\"Avatar updated.\"}").build();
    }

    @DELETE
    @Path("/me/avatar")
    public Response deleteAvatar() {
        int userId = getAuthenticatedUserId();
        UserPreferences prefs = userPreferencesRepository.findByUserId(userId);
        if (prefs == null) return Response.status(Response.Status.NOT_FOUND).build();

        String current = prefs.getUserAvatar();
        if (!"default.png".equals(current)) {
            new File("/opt/szachuz/avatars/", current).delete();
        }
        prefs.setUserAvatar("default.png");
        userPreferencesRepository.update(prefs);
        return Response.ok("{\"message\":\"Avatar reset.\"}").build();
    }
}