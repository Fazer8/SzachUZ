package lol.szachuz.szachuz.db.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lol.szachuz.szachuz.db.EMF;
import lol.szachuz.szachuz.db.Entities.Leaderboard;

import java.util.List;

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

    /**
     * Znajdź graczy powyżej określonego MMR (przykład prostego zapytania warunkowego)
     */
    public List<Leaderboard> findAboveMmr(int minMmr) {
        try (EntityManager em = EMF.get().createEntityManager()) {
            return em.createQuery("SELECT l FROM Leaderboard l WHERE l.mmr > :mmr", Leaderboard.class)
                    .setParameter("mmr", minMmr)
                    .getResultList();
        }
    }
}
