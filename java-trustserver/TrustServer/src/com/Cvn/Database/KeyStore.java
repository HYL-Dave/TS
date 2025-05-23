package com.Cvn.Database;

import java.sql.Timestamp;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.cache.annotation.Cacheable;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name="keystore")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class KeyStore {

    @Id
    @Column(name = "ALIAS", nullable = false)
    private String alias;
    @Lob
    @Column(name="PRIV_KEY", nullable = false)
    private String prikey;
    @Column(name="PUB_KEY", nullable = false)
    private String pubkey;
    @Column(name="CREATE_DATE", nullable = false)
    @CreationTimestamp
    private Timestamp createDate;

    public Boolean isEmpty() {
        return (pubkey==null && prikey==null);
    }

}
