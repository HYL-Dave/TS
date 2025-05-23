package com.Cvn.Database;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Getter
@Setter
@ToString
@Entity
@Table(name="channel_trust")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ChannelTrust {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHANNEL_ID")
    private String channelID;
    @Column(name="CHANNEL_NAME")
    private String channelName;
    @Column(name="OTP_LENGTH", nullable = false)
    private Integer otpLength;
    @Column(name="OTP_INTERVAL", nullable = false)
    private Integer otpIntrval;
    @Column(name="OTP_RANGE", nullable = false)
    private Integer otpRange;
    @Column(name="JNDI")
    private String jndi;
    @Column(name="ACTIVATE")
    private String activate;

    @Column(name="CREATE_DATE", nullable = false)
    @CreationTimestamp
    private Timestamp createDate;

    @Column(name="LAST_MODIFIED")
    @UpdateTimestamp
    private Timestamp modified;
}
