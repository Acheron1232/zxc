package com.laba.ilaba.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class EntityManagerProducer {

    private final EntityManagerFactory emf;

    public EntityManagerProducer() {
        this.emf = Persistence.createEntityManagerFactory("default");
        log.info("EntityManagerFactory created for persistence unit 'default'");
    }

    @Produces
    @RequestScoped
    public EntityManager createEntityManager() {
        EntityManager em = emf.createEntityManager();
        log.debug("EntityManager created");
        return em;
    }

    public void closeEntityManager(@Disposes EntityManager em) {
        if (em.isOpen()) {
            em.close();
            log.debug("EntityManager closed");
        }
    }
}