package lol.szachuz.db.Repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lol.szachuz.db.EMF;
import lol.szachuz.db.Entities.UserPreferences;

import java.util.List;

/**
 * Manages the persistence and retrieval of {@link UserPreferences} entities in the database.
 *
 * This repository provides various methods for CRUD operations on user preferences, allowing
 * the application to save, update, find, and delete user preferences, as well as perform specific
 * queries like finding preferences by language.
 *
 * All operations involving the database are appropriately managed within transactions to ensure
 * data consistency and integrity. Transactions are committed upon successful execution or rolled
 * back in case of errors.
 *
 * Note: This class leverages a centralized {@link EntityManagerFactory} for database interactions,
 * provided by the {@link EMF} utility class.
 */
@ApplicationScoped
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
