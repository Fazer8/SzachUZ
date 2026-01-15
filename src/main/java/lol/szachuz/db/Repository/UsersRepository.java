package lol.szachuz.db.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import lol.szachuz.db.Entities.Users;
import lol.szachuz.db.EMF;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject; // Import dla CDI

import java.util.Optional;

import lol.szachuz.auth.TokenService; // Import serwisu

import java.util.List;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@ApplicationScoped
public class UsersRepository {
    /**
     * Provides access to the TokenService instance, which is used for generating JWT tokens
     * and managing private key operations related to authentication.
     *
     * TokenService handles the creation of secure signed tokens for user authentication,
     * leveraging the application's private key.
     *
     * This field is automatically injected into the UsersRepository class using dependency injection.
     */

    @Inject
    private TokenService tokenService;


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

    public String updateUser(Users user) {
        EntityManager em = EMF.get().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            em.merge(user);
            tx.commit();
            return "User updated successfully";
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            System.err.println("Error updating user: " + e.getMessage());
            return "User update failed";
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
    public Optional<Users> findUserByEmail(String email) {
        try (EntityManager em = EMF.get().createEntityManager()) {
            Users user = em.createQuery(
                            "SELECT u FROM Users u WHERE u.email = :email", Users.class)
                    .setParameter("email", email)
                    .getSingleResult();

            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public String login(String email, String password) {
        try (EntityManager em = EMF.get().createEntityManager()) {

            String hashedPassword;
            try {
                hashedPassword = sha256(password);
            } catch (Exception e) {
                System.err.println("Błąd haszowania: " + e.getMessage());
                return null;
            }

            Users user = em.createQuery(
                            "SELECT u FROM Users u WHERE u.email = :email AND u.password = :password",
                            Users.class
                    )
                    .setParameter("email", email)
                    .setParameter("password", hashedPassword)
                    .getSingleResult(); // <-- Tutaj ryzyko NoResultException, poprawnie łapane poniżej

            // Generowanie tokena JWT
            return tokenService.generateToken(user);

        } catch (NoResultException e) {
            return null; // Nie znaleziono użytkownika lub złe hasło
        } catch (Exception e) {
            System.err.println("Błąd podczas logowania: " + e.getMessage());
            return null;
        }
    }
//  ===->====<-===->==<-REJESTRACJA->==<-===->====<-===

    public String register(String username, String email, String password) {
        EntityManager em = EMF.get().createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            String hashedPassword = sha256(password);

            tx.begin();

            Query query = em.createNativeQuery(
                    "SELECT add_new_user(:username, :email, :password)"
            );
            query.setParameter("username", username);
            query.setParameter("email", email);
            query.setParameter("password", hashedPassword);

            Integer userId = (Integer) query.getSingleResult();
            tx.commit();

            return "Registration successful (userId = " + userId + ")";
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }

            String msg = e.getMessage();
            if (msg != null) {
                if (msg.contains("Nazwa użytkownika")) return "Username already taken";
                if (msg.contains("Email")) return "Email already taken";
            }

            System.err.println("Database error: " + msg);
            return "Registration failed";
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }


    public boolean checkPassword(Users user, String rawPassword) {
        try {
            String hashedInput = sha256(rawPassword);
            return user.getPassword().equals(hashedInput);
        } catch (Exception e) {
            System.err.println("Błąd haszowania podczas sprawdzania hasła: " + e.getMessage());
            return false;
        }
    }

    public void updatePassword(Users user, String newRawPassword) {
        EntityManager em = EMF.get().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            String newHashedPassword = sha256(newRawPassword);

            tx.begin();
            Users managedUser = em.merge(user);

            managedUser.setPassword(newHashedPassword);

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            System.err.println("Błąd aktualizacji hasła dla użytkownika " + user.getUserId() + ": " + e.getMessage());
            throw new RuntimeException("Nie można zmienić hasła.", e);
        }
    }

    private String sha256(String data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
}
