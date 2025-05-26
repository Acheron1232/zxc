package com.laba.ilaba.repository;

import com.laba.ilaba.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Slf4j
public class UserRepository {

    @Inject
    private EntityManager entityManager;

    @Inject
    public UserRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<User> findById(Long id) {
        try {
            User user = entityManager.find(User.class, id);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            log.error("Error finding user by id: {}", id, e);
            return Optional.empty();
        }
    }

    public Optional<User> findByEmail(String email) {
        try {
            User user = entityManager.createQuery(
                    "SELECT u FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error finding user by email: {}", email, e);
            return Optional.empty();
        }
    }

    public List<User> findAll() {
        try {
            return entityManager.createQuery("SELECT u FROM User u", User.class)
                    .getResultList();
        } catch (Exception e) {
            log.error("Error finding all users", e);
            return List.of();
        }
    }

    public User save(User user) {
        try {
            if (user.getId() == null) {
                EntityTransaction transaction = entityManager.getTransaction();
                transaction.begin();
                entityManager.persist(user);
                entityManager.flush();
                transaction.commit();
                return user;
            } else {
                EntityTransaction transaction = entityManager.getTransaction();
                transaction.begin();
                User mergedUser = entityManager.merge(user);
                entityManager.flush();
                transaction.commit();
                return mergedUser;
            }
        } catch (Exception e) {
            log.error("Error saving user: {}", user, e);
            throw e;
        }
    }

    public void delete(User user) {
        try {
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            entityManager.remove(entityManager.contains(user) ? user : entityManager.merge(user));
            entityManager.flush();
            transaction.commit();
        } catch (Exception e) {
            log.error("Error deleting user: {}", user, e);
            throw e;
        }
    }

    public boolean existsByEmail(String email) {
        try {
            Long count = entityManager.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            log.error("Error checking if user exists by email: {}", email, e);
            return false;
        }
    }
}
