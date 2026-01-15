package lol.szachuz.db.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lol.szachuz.db.EMF;
import lol.szachuz.db.Entities.Friends;

import java.util.List;

public class FriendsRepository {
    /**
     * Persists a Friends entity into the database.
     * Ensures the transaction is properly handled by committing on success or rolling back on failure.
     *
     * @param friend the Friends entity to be persisted; must not be null
     * @throws IllegalArgumentException if the argument is not an entity or is not managed
     * @throws Exception if an error occurs during the transaction
     */
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
