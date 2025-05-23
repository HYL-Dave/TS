package com.toppanidgate.idenkey.SMS_Count.model;

import java.sql.Timestamp;

public class SMS_CountVO {
	private long id;
	private long iDGate_ID;
	private String Operation;
	private String Status;
	private int Fail_Count;
	private Timestamp Create_Date = null;
	private Timestamp Last_Modified = null;

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setiDGate_ID(long id) {
		this.iDGate_ID = id;
	}

	public long getiDGate_ID() {
		return iDGate_ID;
	}

	public void setFail_Count(int count) {
		this.Fail_Count = count;
	}

	public int getFail_Count() {
		return Fail_Count;
	}

	public void setOperation(String opt) {
		this.Operation = opt;
	}

	public String getOperation() {
		return Operation;
	}

	public void setStatus(String status) {
		this.Status = status;
	}

	public String getStatus() {
		return Status;
	}

	public void setCreate_Date(Timestamp time) {
		if (time != null)
			this.Create_Date = new Timestamp(time.getTime());
	}

	public Timestamp getCreate_Date() {
		if (Create_Date == null)
			return null;
		
		return new Timestamp(Create_Date.getTime());
	}

	public void setLast_Modified(Timestamp time) {
		if (time != null)
			this.Last_Modified = new Timestamp(time.getTime());
	}

	public Timestamp getLast_Modified() {
		if (Last_Modified == null)
			return null;
		
		return new Timestamp(Last_Modified.getTime());
	}
}
