package lol.szachuz.szachuz.api.dto;

import lol.szachuz.szachuz.db.Entities.UserPreferences;
import lol.szachuz.szachuz.db.Entities.Users;

public class ProfileDTO {

    private long userId;
    private String username;
    private String email;
    private String language;
    private boolean darkMode;
    private String userAvatar;

    public ProfileDTO() {
    }

    public ProfileDTO(Users user, UserPreferences prefs) {
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.email = user.getEmail();

        if (prefs != null) {
            this.language = prefs.getLanguage() != null ? prefs.getLanguage().name() : null;
            this.darkMode = prefs.getDarkMode();
            this.userAvatar = prefs.getUserAvatar();
        }
    }

//    <<  <<  GETTERS/SETTERS  >>  >>

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean getDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }
}