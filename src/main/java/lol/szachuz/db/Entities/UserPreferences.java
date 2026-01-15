package lol.szachuz.db.Entities;

import jakarta.persistence.*;


/**
 * Represents the user preferences associated with a specific user in the system.
 *
 * This entity is mapped to the "userpreferences" table in the database and stores
 * personalized settings for a user, such as language, UI appearance, and avatar.
 * It is linked to the "users" table via a one-to-one relationship.
 */
@Entity
@Table(name = "userpreferences")
public class UserPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "userid")
    private Users user;

    @Enumerated(EnumType.STRING)
    private Lang language;
    private boolean darkMode;
    private String userAvatar;


    //    <<  <<  GETTERS/SETTERS  >>  >>

    public Users getUser() {
        return user;
    }

    public Lang getLanguage() {
        return language;
    }

    public void setLanguage(Lang language) {
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

    public enum Lang {PL, EN}
}

