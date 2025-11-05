package lol.szachuz.szachuz.db.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import lol.szachuz.szachuz.db.DTO.Users;
import lol.szachuz.szachuz.db.EMF;

import java.util.List;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;


public class UsersRepository {

    public void save(Users user) {
        EntityManager em = EMF.get().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            em.persist(user);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    public boolean existsByEmail(String email) {
        try (EntityManager em = EMF.get().createEntityManager()) {
            return em.createQuery("SELECT u FROM Users u WHERE u.email = :email", Users.class)
                    .setParameter("email", email)
                    .getResultStream()
                    .findFirst()
                    .isPresent();
        }
    }

    public boolean existsByUsername(String username) {
        try (EntityManager em = EMF.get().createEntityManager()) {
            return em.createQuery("SELECT u FROM Users u WHERE u.username = :username", Users.class)
                    .setParameter("username", username)
                    .getResultStream()
                    .findFirst()
                    .isPresent();
        }
    }
    public Users findByEmail(String email) {
        try (EntityManager em = EMF.get().createEntityManager()) {
            return em.createQuery("SELECT u FROM Users u WHERE u.email = :email", Users.class)
                    .setParameter("email", email)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        }
    }

    public Users findById(int id) {
        try (EntityManager em = EMF.get().createEntityManager()) {
            return em.find(Users.class, id);
        }
    }

    public List<Users> findAll() {
        try (EntityManager em = EMF.get().createEntityManager()) {
            return em.createQuery("SELECT u FROM Users u", Users.class)
                    .getResultList();
        }
    }

    public void update(Users user) {
        EntityManager em = EMF.get().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            em.merge(user);
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
            Users user = em.find(Users.class, id);
            if (user != null) {
                em.remove(user);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
//  ===->====<-===->==<-LOGOWANIE->==<-===->====<-===

    public String login(String email, String password) {
        EntityManager em = EMF.get().createEntityManager();
        try {
            Users user = em.createQuery(
                            "SELECT u FROM Users u WHERE u.email = :email AND u.password = :password",
                            Users.class
                    )
                    .setParameter("email", email)
                    .setParameter("password", password)
                    .getSingleResult();

            String tokenData = user.getUserId() + ":" + Instant.now().toEpochMilli();
            String token = Base64.getEncoder().encodeToString(sha256(tokenData).getBytes(StandardCharsets.UTF_8));

            return token;

        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }
//  ===->====<-===->==<-REJESTRACJA->==<-===->====<-===

    public String register(String username, String email, String password) {
        if (existsByEmail(email)) {
            return "Email already exists";
        }

        EntityManager em = EMF.get().createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            String hashedPassword = sha256(password);

            Users newUser = new Users();
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setPassword(hashedPassword);

            tx.begin();
            em.persist(newUser);
            tx.commit();

            return "Registration successful";
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            System.err.println(e.getMessage());
            return "Registration failed";
        } finally {
            em.close();
        }
    }

    private String sha256(String data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
}
