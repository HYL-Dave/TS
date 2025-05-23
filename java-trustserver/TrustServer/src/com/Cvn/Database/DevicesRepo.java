package com.Cvn.Database;

import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DevicesRepo extends JpaRepository<Devices, Long> {
        List<Devices> findAll();
        @QueryHints(
                value = { @QueryHint(name = "org.hibernate.cacheable", value = "true")},
                forCounting = false)
        Devices findByEsn(String esn);

        @QueryHints(
                value = { @QueryHint(name = "org.hibernate.cacheable", value = "true")},
                forCounting = false)
        Devices findByUserID(String userID);

        @Query(value = "call usp_getNewEsnSeq();", nativeQuery = true)
        String getNextESN();
}