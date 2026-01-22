package lol.szachuz.db.Repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import lol.szachuz.db.EMF;
import lol.szachuz.db.Entities.Users;

@ApplicationScoped
public class EmailRepository {

    public String getEmailByUserId(int userId) {
        try (EntityManager em = EMF.get().createEntityManager()) {
            try {
                return em.createQuery("SELECT u.email FROM Users u WHERE u.userId = :uid", String.class)
                        .setParameter("uid", userId)
                        .getSingleResult();
            } catch (Exception e) {
                return null;
            }
        }
    }

    public String getUsernameByUserId(int userId) {
        try (EntityManager em = EMF.get().createEntityManager()) {
            try {
                return em.createQuery("SELECT u.username FROM Users u WHERE u.userId = :uid", String.class)
                        .setParameter("uid", userId)
                        .getSingleResult();
            } catch (Exception e) {
                return "Gracz " + userId;
            }
        }
    }
}