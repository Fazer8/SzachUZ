package lol.szachuz.api;

import java.io.File;
import java.util.Base64;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.HttpHeaders;

import lol.szachuz.db.Entities.Users;
import lol.szachuz.api.dto.ProfileDTO;
import lol.szachuz.api.dto.ChangePasswordDTO;
import lol.szachuz.db.Entities.UserPreferences;
import lol.szachuz.db.Repository.UsersRepository;
import lol.szachuz.db.Repository.UserPreferencesRepository;

import jakarta.inject.Inject;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.json.bind.annotation.JsonbProperty;

import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;


@Path("/profile")
@RequestScoped
@RolesAllowed({"USER"})
public class ProfileResource {

    @Inject
    private UsersRepository usersRepository;

    @Inject
    private UserPreferencesRepository userPreferencesRepository;


    @Context
    private HttpHeaders headers;

    private int getAuthenticatedUserId() {
        String authHeader = headers.getHeaderString("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new WebApplicationException("Missing or invalid Authorization header", Response.Status.UNAUTHORIZED);
        }
        try {
            String token = authHeader.substring(7);

            String[] chunks = token.split("\\.");
            if (chunks.length < 2) {
                throw new WebApplicationException("Invalid JWT format", Response.Status.UNAUTHORIZED);
            }
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payloadJson = new String(decoder.decode(chunks[1]), StandardCharsets.UTF_8);
            String searchKey = "\"sub\"";
            int subIndex = payloadJson.indexOf(searchKey);

            if (subIndex != -1) {
                int startQuote = payloadJson.indexOf("\"", subIndex + searchKey.length());
                while (payloadJson.charAt(startQuote) != '"') {
                    startQuote++;
                }
                int endQuote = payloadJson.indexOf("\"", startQuote + 1);

                if (endQuote != -1) {
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
    public Response updateAvatar(
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail,
            @FormDataParam("filename") String manualFilename
    ) {
        if (uploadedInputStream == null || fileDetail == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("File is required.").build();
        }

        int userId = getAuthenticatedUserId();
        UserPreferences prefs = userPreferencesRepository.findByUserId(userId);
        if (prefs == null) return Response.status(Response.Status.NOT_FOUND).build();

        String originalName = (manualFilename != null && !manualFilename.isEmpty())
                ? manualFilename
                : fileDetail.getFileName();

        if (originalName == null) originalName = "avatar.png";

        String ext = "png";
        if (originalName.toLowerCase().endsWith(".jpg") || originalName.toLowerCase().endsWith(".jpeg")) ext = "jpg";

        String avatarsDir = "/opt/szachuz/avatars/";
        new File(avatarsDir).mkdirs();

        String filename = "avatar_" + userId + "." + ext;
        File targetFile = new File(avatarsDir, filename);

        try {
            java.nio.file.Files.copy(
                    uploadedInputStream,
                    targetFile.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Failed to save file: " + e.getMessage()).build();
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
        if (!"default_avatar.png".equals(current)) {
            new File("/opt/szachuz/avatars/", current).delete();
        }
        prefs.setUserAvatar("default_avatar.png");
        userPreferencesRepository.update(prefs);
        return Response.ok("{\"message\":\"Avatar reset.\"}").build();
    }


    @GET
    @Path("/avatars/{filename}")
    @Produces("image/png")
    public Response getAvatar(@PathParam("filename") String filename) {
        if (filename == null || filename.contains("..") || filename.contains("/")) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        java.io.File file = new java.io.File("/opt/szachuz/avatars/" + filename);
        if (!file.exists()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(file).header("Content-Disposition", "inline; filename=\"" + filename + "\"").build();
    }
}