package com.Device_Detail.model;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
	private String Device_Reg_IP;

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
}