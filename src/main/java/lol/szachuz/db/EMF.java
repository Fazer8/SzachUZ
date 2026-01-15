package lol.szachuz.db;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

public class EMF {
    /**
     * A static and final instance of {@link EntityManagerFactory}.
     * This variable is initialized during class loading and is used to manage
     * entity manager instances for persistence operations in the application.
     *
     * The factory is created using properties populated with database credentials
     * fetched from environment variables. If these credentials are not provided,
     * an error is logged, and the factory initialization may fail.
     *
     * It is important to ensure that the factory is properly closed when it is
     * no longer needed to release associated resources.
     */
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
