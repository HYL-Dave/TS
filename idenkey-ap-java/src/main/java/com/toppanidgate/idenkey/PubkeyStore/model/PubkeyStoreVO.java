package com.toppanidgate.idenkey.PubkeyStore.model;

import java.sql.Timestamp;

public class PubkeyStoreVO {

	private long id;
	private long iDGateID;
	private String alias;
	private String device_data;
	private String pub_key_ECC;
	private String pub_key;
	private Timestamp Create_Date = null;
	
	public String getPub_key_ECC() {
		return pub_key_ECC;
	}
	public void setPub_key_ECC(String pub_key_ECC) {
		this.pub_key_ECC = pub_key_ECC;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getiDGateID() {
		return iDGateID;
	}
	public void setiDGateID(long iDGateID) {
		this.iDGateID = iDGateID;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getDevice_data() {
		return device_data;
	}
	public void setDevice_data(String device_data) {
		this.device_data = device_data;
	}
	public String getPub_key() {
		return pub_key;
	}
	public void setPub_key(String pub_key) {
		this.pub_key = pub_key;
	}
	public Timestamp getCreate_Date() {
		return Create_Date;
	}
	public void setCreate_Date(Timestamp create_Date) {
		Create_Date = create_Date;
	}

}