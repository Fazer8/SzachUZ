package lol.szachuz.db.Repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lol.szachuz.db.EMF;
import lol.szachuz.db.Entities.UserPreferences;

import java.util.List;

@ApplicationScoped
public class UserPreferencesRepository {
    /**
     * Persists the provided UserPreferences entity in the database.
     * This method begins a transaction, attempts to save the provided preferences,
     * and commits the transaction upon successful operation. If an exception occurs
     * during the process, the transaction is rolled back.
     *
     * @param preferences the UserPreferences entity to be saved to the database; must not be null.
     * @throws IllegalArgumentException if the provided preferences entity is null.
     * @throws Exception if there is an error during the persist process or transaction management.
     */
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
