package lol.szachuz.api;

import jakarta.ws.rs.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.MediaType;
import lol.szachuz.db.Entities.Users;
import lol.szachuz.api.dto.ProfileDTO;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import org.eclipse.microprofile.jwt.JsonWebToken;
import jakarta.json.bind.annotation.JsonbProperty;
import lol.szachuz.api.dto.ChangePasswordDTO;
import lol.szachuz.db.Entities.UserPreferences;
import lol.szachuz.db.Repository.UsersRepository;
import lol.szachuz.db.Repository.UserPreferencesRepository;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FileOutputStream;


@Path("/profile")
@RequestScoped
@RolesAllowed({"USER"})
public class ProfileResource {

    @Inject
    private UsersRepository usersRepository;

    @Inject
    private UserPreferencesRepository userPreferencesRepository;

    @Inject
    private JsonWebToken jwt;


    private int getAuthenticatedUserId() {
        if (jwt == null) {
            throw new WebApplicationException("Unauthorized", Response.Status.UNAUTHORIZED);
        }
        return Integer.parseInt(jwt.getSubject());
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
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        user.setUsername(dto.value);
        usersRepository.update(user);
        return Response.ok().build();
    }

    @PUT
    @Path("/me/password")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePassword(ChangePasswordDTO dto) {
        // 1. Walidacja danych
        if (dto == null || dto.oldPassword == null || dto.newPassword == null ||
                dto.oldPassword.isEmpty() || dto.newPassword.length() < 8) { // Wymaganie min. 8 znaków dla bezpieczeństwa
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid password data or new password is too short (min 8 characters).").build();
        }

        int userId = getAuthenticatedUserId();
        Users user = usersRepository.findById(userId);

        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!usersRepository.checkPassword(user, dto.oldPassword)) {
            return Response.status(Response.Status.FORBIDDEN).entity("Incorrect old password.").build(); // Używamy FORBIDDEN lub UNAUTHORIZED
        }

        usersRepository.updatePassword(user, dto.newPassword);

        // 4. Sukces
        return Response.ok().entity("Password changed successfully.").build();
    }


    @PUT
    @Path("/me/darkMode")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateDarkMode(UpdateFieldDTO dto) {
        if (dto == null || dto.darkMode == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        int userId = getAuthenticatedUserId();
        UserPreferences prefs = userPreferencesRepository.findByUserId(userId);

        if (prefs == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("User preferences not found.").build();
        }

        prefs.setDarkMode(dto.darkMode);
        userPreferencesRepository.update(prefs);
        return Response.ok().build();
    }

    @PUT
    @Path("/me/language")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateLanguage(UpdateFieldDTO dto) {
        if (dto == null || dto.value == null || dto.value.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        int userId = getAuthenticatedUserId();
        UserPreferences prefs = userPreferencesRepository.findByUserId(userId);

        if (prefs == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("User preferences not found.").build();
        }

        try {
            prefs.setLanguage(UserPreferences.Lang.valueOf(dto.value.toUpperCase()));

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid language value. Expected one of: PL, EN, etc.").build();
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
        if (uploadedInputStream == null || originalFilename == null || originalFilename.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("File is required.").build();
        }

        int userId = getAuthenticatedUserId();

        UserPreferences prefs = userPreferencesRepository.findByUserId(userId);
        if (prefs == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("User preferences not found.").build();
        }

        // Wyciągnięcie rozszerzenia
        String ext = "png";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex >= 0 && dotIndex < originalFilename.length() - 1) {
            ext = originalFilename.substring(dotIndex + 1).toLowerCase();
            if (ext.equals("jpeg")) ext = "jpg";
        }

        // Katalog zewnętrzny
        String avatarsDir = "/opt/szachuz/avatars/";
        File dir = new File(avatarsDir);
        if (!dir.exists()) dir.mkdirs();

        // Nazwa pliku specyficzna dla użytkownika
        String filename = "avatar_" + userId + "." + ext;
        File avatarFile = new File(dir, filename);

        // Zapis do pliku
        try (OutputStream out = new FileOutputStream(avatarFile)) {
            uploadedInputStream.transferTo(out);
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to save avatar.").build();
        }

        // Zapis w bazie
        prefs.setUserAvatar(filename);
        userPreferencesRepository.update(prefs);

        return Response.ok("{\"message\":\"Avatar updated.\"}").build();
    }


    @DELETE
    @Path("/me/avatar")
    public Response deleteAvatar() {
        int userId = getAuthenticatedUserId();

        UserPreferences prefs = userPreferencesRepository.findByUserId(userId);
        if (prefs == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("User preferences not found.").build();
        }

        String current = prefs.getUserAvatar();
        String defaultAvatar = "default.png";

        if (current == null || current.equals(defaultAvatar)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Cannot delete default avatar.").build();
        }

        String avatarsDir = "/opt/szachuz/avatars/";
        File avatarFile = new File(avatarsDir, current);
        if (avatarFile.exists()) avatarFile.delete();

        prefs.setUserAvatar(defaultAvatar);
        userPreferencesRepository.update(prefs);

        return Response.ok("{\"message\":\"Avatar reset to default.\"}").build();
    }
}
