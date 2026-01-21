package lol.szachuz.db.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lol.szachuz.db.EMF;
import lol.szachuz.db.Entities.Friends;
import jakarta.persistence.EntityManagerFactory;


import java.util.List;

/**
 * Repository class for managing {@link Friends} entities. This class provides
 * methods for common database operations such as saving, retrieving, updating,
 * and deleting {@link Friends} entities, as well as finding friendships
 * between users.
 *
 * All methods interact with the database using an {@link EntityManagerFactory}
 * provided by the {@link EMF} utility class. Transactions are handled properly
 * to ensure that operations are either committed or rolled back in case of failures.
 * Na razie nie używane. zapewne nie będzie
 */
public class FriendsRepository {

    public void save(Friends friend) {
        EntityManager em = EMF.get().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            em.persist(friend);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    public Friends findById(int id) {
        try (EntityManager em = EMF.get().createEntityManager()) {
            return em.find(Friends.class, id);
        }
    }

    public List<Friends> findAll() {
        try (EntityManager em = EMF.get().createEntityManager()) {
            return em.createQuery("SELECT f FROM Friends f", Friends.class)
                    .getResultList();
        }
    }

    public void update(Friends friend) {
        EntityManager em = EMF.get().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            em.merge(friend);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    public void delete(int id) {
        EntityManager em = EMF.get().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            Friends friend = em.find(Friends.class, id);
            if (friend != null) {
                em.remove(friend);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    /**
     * Znajdź przyjaźń między dwoma użytkownikami.
     */
    public Friends findBetweenUsers(int userId1, int userId2) {
        try (EntityManager em = EMF.get().createEntityManager()) {
            return em.createQuery("""
                            SELECT f FROM Friends f
                            WHERE (f.user1.userId = :u1 AND f.user2.userId = :u2)
                               OR (f.user1.userId = :u2 AND f.user2.userId = :u1)
                            """, Friends.class)
                    .setParameter("u1", userId1)
                    .setParameter("u2", userId2)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        }
    }
}
