package lol.szachuz.szachuz.db.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lol.szachuz.szachuz.db.EMF;
import lol.szachuz.szachuz.db.Entities.UserPreferences;

import java.util.List;

public class UserPreferencesRepository {

    public void save(UserPreferences preferences) {
        EntityManager em = EMF.get().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            em.persist(preferences);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    public UserPreferences findByUserId(int userId) {
        try (EntityManager em = EMF.get().createEntityManager()) {
            return em.find(UserPreferences.class, userId);
        }
    }

    public List<UserPreferences> findAll() {
        try (EntityManager em = EMF.get().createEntityManager()) {
            return em.createQuery("SELECT p FROM UserPreferences p", UserPreferences.class)
                    .getResultList();
        }
    }

    public void update(UserPreferences preferences) {
        EntityManager em = EMF.get().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            em.merge(preferences);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    public void delete(int userId) {
        EntityManager em = EMF.get().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            UserPreferences pref = em.find(UserPreferences.class, userId);
            if (pref != null) {
                em.remove(pref);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    public List<UserPreferences> findByLanguage(String langCode) {
        try (EntityManager em = EMF.get().createEntityManager()) {
            return em.createQuery(
                            "SELECT p FROM UserPreferences p WHERE p.language = :lang",
                            UserPreferences.class
                    )
                    .setParameter("lang", Enum.valueOf(UserPreferences.Lang.class, langCode))
                    .getResultList();
        }
    }
}
