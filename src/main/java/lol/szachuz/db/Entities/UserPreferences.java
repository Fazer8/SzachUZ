package lol.szachuz.db.Entities;

import jakarta.persistence.*;

@Entity
@Table(name = "userpreferences")
public class UserPreferences {
    /**
     * Represents the unique identifier for a user within the system.
     *
     * This field serves as the primary key in the "userpreferences" table
     * and is linked to the corresponding primary key in the "users" table
     * through a one-to-one relationship.
     *
     * It is automatically generated using the IDENTITY strategy, ensuring
     * its uniqueness and consistency across the database.
     */
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

