package lol.szachuz.matchmaking;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DatabaseService {

    private static final String DB_URL = "jdbc:postgresql://postgres:5432/postgres";
    private static final String DB_USER = "postgres";
    // Upewnij się, że hasło jest wpisane poprawnie
    private static final String DB_PASS = "vifon_kurczak_curry";

    public static int getUserMMR(int userId) {
        System.out.println("--- DIAGNOSTYKA DB: Pobieranie MMR dla gracza " + userId + " ---");

        // POPRAWKA: 'rating' -> 'mmr' ORAZ 'user_id' -> 'userid'
        String sql = "SELECT mmr FROM leaderboard WHERE userid = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // POPRAWKA: pobieramy kolumnę "mmr"
                    int mmr = rs.getInt("mmr");
                    System.out.println("--- DIAGNOSTYKA DB: Znaleziono MMR: " + mmr + " ---");
                    return mmr;
                }
            }
        } catch (Exception e) {
            System.err.println("--- DIAGNOSTYKA DB BŁĄD: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("--- DIAGNOSTYKA DB: Brak MMR w bazie (lub błąd), zwracam domyślne 1000 ---");
        return 1000;
    }
}