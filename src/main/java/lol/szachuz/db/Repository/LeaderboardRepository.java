package lol.szachuz.db.Repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lol.szachuz.api.dto.LeaderboardDTO;
import lol.szachuz.db.EMF;
import lol.szachuz.db.Entities.Leaderboard;

import java.util.List;

@ApplicationScoped
public class LeaderboardRepository {
    /**
     * Persists the provided Leaderboard object into the database. This method uses
     * an EntityManager to initiate a transaction, persist the entity, and commit the transaction.
     * If an exception occurs during the process, the transaction is rolled back, and the exception
     * is propagated to the caller.
     *
     * @param leaderboard the Leaderboard object to be persisted in the database
     * @throws RuntimeException if an error occurs during the persistence process
     */
    public void save(Leaderboard leaderboard) {
        EntityManager em = EMF.get().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            em.persist(leaderboard);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    public Leaderboard findByUserId(int userId) {
        try (EntityManager em = EMF.get().createEntityManager()) {
            return em.find(Leaderboard.class, userId);
        }
    }

    public List<Leaderboard> findAll() {
        try (EntityManager em = EMF.get().createEntityManager()) {
            return em.createQuery("SELECT l FROM Leaderboard l", Leaderboard.class)
                    .getResultList();
        }
    }

    public List<LeaderboardDTO> findTop10() {
        try (EntityManager em = EMF.get().createEntityManager()) {
            return em.createQuery(
                            "SELECT new lol.szachuz.api.dto.LeaderboardDTO(l.user.username, l.mmr, l.matchesWon) FROM Leaderboard l ORDER BY l.mmr DESC", LeaderboardDTO.class)
                    .setMaxResults(10)
                    .getResultList();
        }
    }


    public void update(Leaderboard leaderboard) {
        EntityManager em = EMF.get().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            em.merge(leaderboard);
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
            Leaderboard lb = em.find(Leaderboard.class, userId);
            if (lb != null) {
                em.remove(lb);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    public List<Leaderboard> findAboveMmr(int minMmr) {
        try (EntityManager em = EMF.get().createEntityManager()) {
            return em.createQuery("SELECT l FROM Leaderboard l WHERE l.mmr > :mmr", Leaderboard.class)
                    .setParameter("mmr", minMmr)
                    .getResultList();
        }
    }
}
