package lol.szachuz.db;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class EMF {
    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("myPU");

    public static EntityManagerFactory get() {
        return emf;
    }

    public static void close() {
        emf.close();
    }
}
