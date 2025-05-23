package com.toppanidgate.idenkey.Channel.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name="Channel")
@Data
public class ChannelVO {
	@Id
	private Long id;
	@Column(name = "Channel_Code", nullable=false)
	private String channel;
	@Column(name = "Channel_Name", nullable=false)
	private String channelName;
	@Column(name = "Activate", nullable=false)
	private String activate;
	private String JNDI;
	private Timestamp Create_Date;
	private Timestamp Last_Modified;

}
