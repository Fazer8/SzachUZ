package lol.szachuz.db.Repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lol.szachuz.api.dto.LeaderboardDTO;
import lol.szachuz.db.EMF;
import lol.szachuz.db.Entities.Leaderboard;

import java.util.List;

/**
 * The LeaderboardRepository class provides database access methods for managing
 * leaderboard entries. It performs operations such as persisting, retrieving,
 * updating, and deleting leaderboard entries, as well as retrieving specific
 * subsets of leaderboard data.
 *
 * This class utilizes an {@link EntityManager} for transaction management and
 * database communication, provided through the {@link EMF#get()} method.
 *
 * Methods in this class are designed to handle exceptions gracefully, rolling
 * back transactions in the event of failure to maintain database consistency.
 */
@ApplicationScoped
public class LeaderboardRepository {

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
