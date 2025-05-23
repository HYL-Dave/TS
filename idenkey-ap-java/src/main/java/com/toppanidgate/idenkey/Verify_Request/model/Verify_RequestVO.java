package com.toppanidgate.idenkey.Verify_Request.model;

import java.sql.Timestamp;

public class Verify_RequestVO {

	private long iDGate_ID;
	private String Request_ID;
	private String Verify_Type;
	private String Status_Code;
	private String Auth_Mode;
	private String Transaction_Name;
	private String Return_Data;
	private Timestamp Transaction_Date = null;
	private String Channel_Code;
	private Timestamp DB_Time = null;
	private String Device_OS;

	public String getDevice_OS() {
		return Device_OS;
	}

	public void setDevice_OS(String device_OS) {
		Device_OS = device_OS;
	}

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

	public String getVerify_Type() {
		return Verify_Type;
	}

	public void setVerify_Type(String type) {
		this.Verify_Type = type;
	}

	public String getStatus_Code() {
		return Status_Code;
	}

	public void setStatus_Code(String status_Code) {
		this.Status_Code = status_Code;
	}

	public String getAuth_Mode() {
		return Auth_Mode;
	}

	public void setAuth_Mode(String mode) {
		this.Auth_Mode = mode;
	}
	
	public String getTransaction_Name() {
		return Transaction_Name;
	}

	public void setTransaction_Name(String name) {
		this.Transaction_Name = name;
	}

	public String getReturn_Data() {
		return Return_Data;
	}

	public void setReturn_Data(String return_Data) {
		this.Return_Data = return_Data;
	}

	public String getChannel_Code() {
		return Channel_Code;
	}

	public void setChannel_Code(String channel) {
		this.Channel_Code = channel;
	}

	public Timestamp getTransaction_Date() {
		return Transaction_Date;
	}

	public void setTransaction_Date(Timestamp transaction_Date) {
		Transaction_Date = transaction_Date;
	}

	public Timestamp getDB_Time() {
		return DB_Time;
	}

	public void setDB_Time(Timestamp time) {
		DB_Time = time;
	}
}