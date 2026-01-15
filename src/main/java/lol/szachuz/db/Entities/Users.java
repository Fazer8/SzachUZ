package lol.szachuz.db.Entities;


import jakarta.persistence.*;

/**
 * Represents a user in the system.
 *
 * This entity is mapped to the "users" table in the database and stores
 * the user's primary details, such as their unique ID, username, email,
 * and their hashed or plain password. The class provides getter and setter
 * methods for each field to allow manipulation of a user's data.
 *
 * Relationships with other entities:
 * - This class is referenced in several other entities, such as Friends,
 *   Leaderboard, and UserPreferences, to establish various associations
 *   including friendships, leaderboard statistics, and user-specific preferences.
 *
 * Fields:
 * - userId: Unique identifier for the user.
 * - username: User's chosen name for identification.
 * - email: User's email address.
 * - password: User's password (usually hashed for security purposes).
 */
@Entity
@Table(name = "users")
public class Users {

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
