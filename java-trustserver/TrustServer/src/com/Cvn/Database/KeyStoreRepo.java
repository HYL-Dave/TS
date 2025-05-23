package com.Cvn.Database;

import jakarta.persistence.QueryHint;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KeyStoreRepo extends JpaRepository<KeyStore, Long> {
        List<KeyStore> findAll();
        @QueryHints(
                value = { @QueryHint(name = "org.hibernate.cacheable", value = "true")},
                forCounting = false)
        KeyStore findByAlias(String alias);
}