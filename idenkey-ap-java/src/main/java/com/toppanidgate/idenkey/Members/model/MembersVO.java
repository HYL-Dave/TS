package com.toppanidgate.idenkey.Members.model;

import java.sql.Timestamp;

public class MembersVO {

	private long iDGateID;
	private String Bank_ID;
	private String Customer_Name;
	private String Mobile_Name;
	private String Account;
	private String Pcode;
	private String Channel_Code;
	private String Customer_Status;
	private String Pref_Lang;
	private int Msg_Count;
	private int Digital_Fails;
	private int Pattern_Fails;
	private int Auth_Fails;
	private Timestamp Create_Date = null;
	private Timestamp Last_Modified = null;
	
	public int getAuth_Fails() {
		return Auth_Fails;
	}

	public void setAuth_Fails(int auth_Fails) {
		Auth_Fails = auth_Fails;
	}

	public long getiDGate_ID() {
		return iDGateID;
	}

	public void setiDGate_ID(long id) {
		this.iDGateID = id;
	}

	public String get_BankID() {
		return Bank_ID;
	}

	public void setBank_ID(String id) {
		this.Bank_ID = id;
	}

	public String getCustomer_Name() {
		return Customer_Name;
	}

	public void setCustomer_Name(String customer_Name) {
		this.Customer_Name = customer_Name;
	}

	public String getMobile_Name() {
		return Mobile_Name;
	}

	public void setMobile_Name(String mobile_Phone) {
		this.Mobile_Name = mobile_Phone;
	}

	public String getAccount() {
		return Account;
	}

	public void setAccount(String email) {
		this.Account = email;
	}

	public String getPcode() {
		return Pcode;
	}

	public void setPcode(String psd) {
		this.Pcode = psd;
	}

	public String getChannel_Code() {
		return Channel_Code;
	}

	public void setChannel_Code(String channel_Code) {
		this.Channel_Code = channel_Code;
	}

	public String getCustomer_Status() {
		return Customer_Status;
	}

	public void setCustomer_Status(String customer_Status) {
		this.Customer_Status = customer_Status;
	}

	public String getPref_Lang() {
		return Pref_Lang;
	}

	public void setPref_Lang(String lang) {
		this.Pref_Lang = lang;
	}
	
	public int getMsg_Count() {
		return Msg_Count;
	}

	public void setMsg_Count(int count) {
		this.Msg_Count = count;
	}
	
	public int getDigital_Fails() {
		return Digital_Fails;
	}

	public void setDigital_Fails(int count) {
		this.Digital_Fails = count;
	}
	
	public int getPattern_Fails() {
		return Pattern_Fails;
	}

	public void setPattern_Fails(int count) {
		this.Pattern_Fails = count;
	}

	public Timestamp getCreate_Date() {
		if (Create_Date == null)
			return null;

		return new Timestamp(Create_Date.getTime());
	}

	public void setCreate_Date(Timestamp create_Date) {
		if (create_Date != null)
			this.Create_Date = new Timestamp(create_Date.getTime());
	}

	public Timestamp getLast_Modified() {
		if (Last_Modified == null)
			return null;

		return new Timestamp(Last_Modified.getTime());
	}

	public void setLast_Modified(Timestamp last_Modified) {
		if (last_Modified != null)
			this.Last_Modified = new Timestamp(last_Modified.getTime());
	}
}