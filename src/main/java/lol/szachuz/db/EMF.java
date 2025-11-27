package lol.szachuz.db;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

public class EMF {
    private static final EntityManagerFactory emf;

    static {
        Map<String, String> properties = new HashMap<>();

        String user = System.getenv("DB_USER");
        String pass = System.getenv("DB_PASSWORD");

        if (user == null || pass == null) {
            System.err.println("Brak wymaganych zmiennych środowiskowych PG_ADMIN_USER/PG_ADMIN_PASSWD");
        } else {
            properties.put("jakarta.persistence.jdbc.user", user);
            properties.put("jakarta.persistence.jdbc.password", pass);
        }

        EntityManagerFactory tmp = null;
        try {
            tmp = Persistence.createEntityManagerFactory("default", properties);
        } catch (Exception e) {
            System.err.println("Błąd inicjalizacji EntityManagerFactory: " + e.getMessage());
//            e.printStackTrace();
        }
        emf = tmp;
    }

    public static EntityManagerFactory get() {
        if (emf == null) {
            throw new IllegalStateException("EntityManagerFactory nie został poprawnie zainicjalizowany");
        }
        return emf;
    }

    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
