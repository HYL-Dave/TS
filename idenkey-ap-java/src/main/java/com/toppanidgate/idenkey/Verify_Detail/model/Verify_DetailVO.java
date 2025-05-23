package com.toppanidgate.idenkey.Verify_Detail.model;

import java.sql.Timestamp;

public class Verify_DetailVO {

	private long iDGate_ID;
	private String Request_ID;
	private String Channel_Code;
	private String Verify_Method;
	private String Transaction_Name;
	private String Transaction_Content;
	private String Challenge;
	private String Callback;
	private String Transaction_Data;
	private String Transaction_Hash;
	private Timestamp Transaction_Date;
	private String encAuthReq;
	private String serverTime;
	private String randomEncTxnData;
	private String randomEncAuthReq;

	public long getiDGate_ID() {
		return iDGate_ID;
	}

	public void setiDGate_ID(long id) {
		this.iDGate_ID = id;
	}

	public String getRequest_ID() {
		return Request_ID;
	}

	public void setRequest_ID(String request_ID) {
		this.Request_ID = request_ID;
	}

	public String getChannel_Code() {
		return Channel_Code;
	}

	public void setChannel_Code(String request_Channel) {
		this.Channel_Code = request_Channel;
	}

	public String getVerify_Method() {
		return Verify_Method;
	}

	public void setVerify_Method(String verify_Method) {
		this.Verify_Method = verify_Method;
	}

	public String getTransaction_Name() {
		return Transaction_Name;
	}

	public void setTransaction_Name(String transaction_Name) {
		this.Transaction_Name = transaction_Name;
	}

	public String getTransaction_Content() {
		return Transaction_Content;
	}

	public void setTransaction_Content(String transaction_Content) {
		this.Transaction_Content = transaction_Content;
	}

	public String getChallenge() {
		return Challenge;
	}

	public void setChallenge(String challenge) {
		this.Challenge = challenge;
	}
	
	public String getCallback() {
		return Callback;
	}

	public void setCallback(String callback) {
		this.Callback = callback;
	}

	public String getTransaction_Data() {
		return Transaction_Data;
	}

	public void setTransaction_Data(String transaction_Data) {
		this.Transaction_Data = transaction_Data;
	}

	public String getTransaction_Hash() {
		return Transaction_Hash;
	}

	public void setTransaction_Hash(String hash) {
		this.Transaction_Hash = hash;
	}

	public Timestamp getTransaction_Date() {
		return Transaction_Date;
	}

	public void setTransaction_Date(Timestamp date) {
		this.Transaction_Date = date;
	}

	public String getEncAuthReq() {
		return encAuthReq;
	}

	public void setEncAuthReq(String encAuthReq) {
		this.encAuthReq = encAuthReq;
	}

	public String getServerTime() {
		return serverTime;
	}

	public void setServerTime(String serverTime) {
		this.serverTime = serverTime;
	}

	public String getRandomEncTxnData() {
		return randomEncTxnData;
	}

	public void setRandomEncTxnData(String randomEncTxnData) {
		this.randomEncTxnData = randomEncTxnData;
	}

	public String getRandomEncAuthReq() {
		return randomEncAuthReq;
	}

	public void setRandomEncAuthReq(String randomEncAuthReq) {
		this.randomEncAuthReq = randomEncAuthReq;
	}
}
