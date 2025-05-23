package com.Cvn.Database;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name="devices")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Devices {
    @Id
    //@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "esn_generator")
    //@SequenceGenerator(name="esn_generator", sequenceName = "ESNTAIL")
    @Column(name = "ESN", nullable = false)
    private String esn;

    @Column(name="USERID", nullable = false)
    private String userID;

    @Column(name = "DEVDATA")
    private String devData;

    @Column(name = "MERCURY")
    private String mercury;

    @Column(name="REGDATE", nullable = false)
    @CreationTimestamp
    private Timestamp regDate;

    @Column(name="MODIFIED")
    @UpdateTimestamp
    private Timestamp modified;

    @Column(name="ERRCOUNT", nullable = false, columnDefinition = "integer default 0")
    private Integer errCount;

    @Column(name="ERRMAX", nullable = false, columnDefinition = "integer default 10")
    private Integer errMax;

    @Column(name="STATUS",nullable = false, columnDefinition = "varchar(1) default '0'")
    private String status;

    @Column(name="DEVID")
    private String devID;

    @Column(name="CHKCOUNT", nullable = false, columnDefinition = "integer default 0")
    private Integer chkCount;

    @Column(name="AUTHTYPE", columnDefinition = "varchar(10) default '1'")
    private String authType;

    @Column(name = "XPH")
    private String xph;
}
