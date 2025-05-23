package com.Channel.model;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChannelVO {
	private String CHANNEL_CODE;
	private String CHANNEL_NAME;
	private String ACTIVATE;
	private int OFFLINE_OTP_lIMIT;
	private int QUICK_LOGIN_PATTERN_LIMIT;
	private int QUICK_LOGIN_PIN_LIMIT;
	private int TXN_PATTERN_LIMIT;
	private int TXN_PIN_LIMIT;
	private Timestamp CREATE_DATE;
	private Timestamp LAST_MODIFIED;
}
