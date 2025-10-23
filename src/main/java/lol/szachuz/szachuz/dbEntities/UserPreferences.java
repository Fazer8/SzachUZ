package lol.szachuz.szachuz.dbEntities;

import jakarta.persistence.*;

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
}

enum Lang {pl, en}
