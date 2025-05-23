package com.Members.model;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MembersVO {

	private long iDGateID;
	private String Device_ID;
	private String Customer_Name;
	private String Mobile_Phone;
	private String Email;
	private String Verify_Type;
	private String Channel_Code;
	private String Customer_Status;
	private String Pref_Lang;
	private int Msg_Count;
	private int Txn_Auth_Fails;
	private int Login_Auth_Fails;
	private int Offline_Auth_Fails;
	private Timestamp Create_Date = null;
	private Timestamp Last_Modified = null;

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