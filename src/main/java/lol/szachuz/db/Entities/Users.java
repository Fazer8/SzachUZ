package lol.szachuz.db.Entities;


import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class Users {
    /**
     * Represents the unique identifier for a user in the system.
     *
     * This field is auto-generated using the IDENTITY strategy for primary key generation.
     * It serves as the primary key for the "users" table in the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private int userId;
    private String username;
    private String email;
    private String password;


//    <<  <<  GETTERS/SETTERS  >>  >>

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
