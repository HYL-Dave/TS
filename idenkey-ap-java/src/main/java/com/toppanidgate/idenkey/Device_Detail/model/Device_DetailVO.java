package com.toppanidgate.idenkey.Device_Detail.model;

import java.sql.Timestamp;

public class Device_DetailVO {

	private long IdgateID;
	private String ESN;
	private String New_ESN;
	private String Device_Data;
	private String Device_ID;
	private String Device_Type;
	private String Device_OS;
	private String Device_OS_Ver;
	private String APP_Ver;
	private String Lang;
	private String Perso_Update;
	private String DeviceLabel;
	private String DeviceModel;
	private Timestamp Modified_Date = null;
	private Timestamp Create_Date = null;
	private Timestamp Persofile_Date = null;
	private int AB_Count;
	private String Auth_Type;
	private String Digital_Hash;
	private String Pattern_Hash;
	private String Bio_Hash;
	private String Device_Reg_IP;
	private String Transaction_ID;

	public long getIdgateID() {
		return IdgateID;
	}

	public void setIdgateID(long id) {
		this.IdgateID = id;
	}

	public String getESN() {
		return ESN;
	}

	public void setESN(String eSN) {
		this.ESN = eSN;
	}
	
	public String getNew_ESN() {
		return New_ESN;
	}

	public void setNew_ESN(String eSN) {
		this.New_ESN = eSN;
	}

	public String getDevice_Data() {
		return Device_Data;
	}

	public void setDevice_Data(String device_Data) {
		this.Device_Data = device_Data;
	}

	public String getDevice_ID() {
		return Device_ID;
	}

	public void setDevice_ID(String device_ID) {
		this.Device_ID = device_ID;
	}

	public String getDevice_Type() {
		return Device_Type;
	}

	public void setDevice_Type(String device_Type) {
		this.Device_Type = device_Type;
	}

	public String getDevice_OS() {
		return Device_OS;
	}

	public void setDevice_OS(String device_OS) {
		this.Device_OS = device_OS;
	}
	
	public String getLang() {
		return Lang;
	}

	public void setLang(String lang) {
		this.Lang = lang;
	}
	
	public String getDevice_OS_Ver() {
		return Device_OS_Ver;
	}

	public void setDevice_OS_Ver(String ver) {
		this.Device_OS_Ver = ver;
	}

	public String getAPP_Ver() {
		return APP_Ver;
	}

	public void setAPP_Ver(String ver) {
		this.APP_Ver = ver;
	}

	public int getAB_Count() {
		return AB_Count;
	}

	public void setAB_Count(int count) {
		this.AB_Count = count;
	}

	public String getPerso_Update() {
		return Perso_Update;
	}

	public void setPerso_Update(String perso_Update) {
		this.Perso_Update = perso_Update;
	}

	public String getDeviceLabel() {
		return DeviceLabel;
	}

	public void setDeviceLabel(String deviceLabel) {
		this.DeviceLabel = deviceLabel;
	}

	public String getDeviceModel() {
		return DeviceModel;
	}

	public void setDeviceModel(String deviceModel) {
		this.DeviceModel = deviceModel;
	}
	
	public String getAuth_Type() {
		return Auth_Type;
	}

	public void setAuth_Type(String type) {
		this.Auth_Type = type;
	}
	
	public String getDigital_Hash() {
		return Digital_Hash;
	}

	public void setDigital_Hash(String hash) {
		this.Digital_Hash = hash;
	}
	
	public String getPattern_Hash() {
		return Pattern_Hash;
	}

	public void setPattern_Hash(String hash) {
		this.Pattern_Hash = hash;
	}
	
	public String getBio_Hash() {
		return Bio_Hash;
	}

	public void setBio_Hash(String hash) {
		this.Bio_Hash = hash;
	}

	public String getDevice_Reg_IP() {
		return Device_Reg_IP;
	}

	public void setDevice_Reg_IP(String ip) {
		this.Device_Reg_IP = ip;
	}

	
	public Timestamp getModified_Date() {
		if (Modified_Date == null)
			return null;

		return new Timestamp(Modified_Date.getTime());
	}

	public void setModified_Date(Timestamp modified_Date) {
		if (modified_Date != null)
			this.Modified_Date = new Timestamp(modified_Date.getTime());
	}

	public Timestamp getCreate_Date() {
		if (Create_Date == null)
			return null;

		return new Timestamp(Create_Date.getTime());
	}

	public void setCreate_Date(Timestamp date) {
		if (date != null)
			this.Create_Date = new Timestamp(date.getTime());
	}
	
	public Timestamp getPersofile_Date() {
		if (Persofile_Date == null)
			return null;

		return new Timestamp(Persofile_Date.getTime());
	}

	public void setPersofile_Date(Timestamp date) {
		if (date != null)
			this.Persofile_Date = new Timestamp(date.getTime());
	}

	public String getTransaction_ID() {
		return Transaction_ID;
	}

	public void setTransaction_ID(String transaction_ID) {
		Transaction_ID = transaction_ID;
	}
}