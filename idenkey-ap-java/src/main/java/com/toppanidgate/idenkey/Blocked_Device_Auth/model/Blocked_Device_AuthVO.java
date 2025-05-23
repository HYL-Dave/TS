package com.toppanidgate.idenkey.Blocked_Device_Auth.model;

public class Blocked_Device_AuthVO {
	private String Auth_Type;
	private String Blocked;
	private String Channel;
	private String Device_Label;
	private String Device_Model;
	private String Create_Date;
	private String Last_Modified;
	
	public void setAuth_Type(String type) {
		Auth_Type = type;
	}
	
	public String getAuth_Type() {
		return Auth_Type;
	}
	
	public void setBlocked(String mode) {
		Blocked = mode;
	}
	
	public String getBlocked() {
		return Blocked;
	}
	
	public void setChannel(String channel) {
		Channel = channel;
	}
	
	public String Channel() {
		return Channel;
	}
	
	public void setDevice_Label(String label) {
		Device_Label = label;
	}
	
	public String getDevice_Label() {
		return Device_Label;
	}
	
	public void setDevice_Model(String model) {
		Device_Model = model;
	}
	
	public String getDevice_Model() {
		return Device_Model;
	}
	
	public void setCreate_Date(String date) {
		Create_Date = date;
	}
	
	public String getCreate_Date() {
		return Create_Date;
	}
	
	public void setLast_Modified(String date) {
		Last_Modified = date;
	}
	
	public String getLast_Modified() {
		return Last_Modified;
	}
}
