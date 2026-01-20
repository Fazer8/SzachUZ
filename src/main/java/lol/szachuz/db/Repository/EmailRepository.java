package lol.szachuz.db.Repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import lol.szachuz.db.EMF;
import lol.szachuz.db.Entities.Users; // <-- Import Twojej encji

@ApplicationScoped
public class EmailRepository {

    public String getEmailByUserId(int userId) {
        try (EntityManager em = EMF.get().createEntityManager()) {
            try {
                // Używamy encji 'Users' i pola 'userId'
                return em.createQuery("SELECT u.email FROM Users u WHERE u.userId = :uid", String.class)
                        .setParameter("uid", userId)
                        .getSingleResult();
            } catch (Exception e) {
                return null;
            }
        }
    }

    // --- NOWA METODA: Pobiera nazwę gracza z bazy ---
    public String getUsernameByUserId(int userId) {
        try (EntityManager em = EMF.get().createEntityManager()) {
            try {
                return em.createQuery("SELECT u.username FROM Users u WHERE u.userId = :uid", String.class)
                        .setParameter("uid", userId)
                        .getSingleResult();
            } catch (Exception e) {
                return "Gracz " + userId; // Fallback, gdyby nie znalazło
            }
        }
    }
}