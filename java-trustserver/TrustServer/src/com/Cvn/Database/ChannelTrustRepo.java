package com.Cvn.Database;

import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

@Repository
public interface ChannelTrustRepo extends JpaRepository<ChannelTrust, Long> {
        List<ChannelTrust> findAll();
        @QueryHints(
                value = { @QueryHint(name = "org.hibernate.cacheable", value = "true")},
                forCounting = false)
        ChannelTrust findByChannelID(String channelID);

        @QueryHints(
                value = { @QueryHint(name = "org.hibernate.cacheable", value = "true")},
                forCounting = false)
        ChannelTrust findByChannelName(String channelName);
}